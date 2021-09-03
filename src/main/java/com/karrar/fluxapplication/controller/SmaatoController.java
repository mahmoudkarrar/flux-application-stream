package com.karrar.fluxapplication.controller;

import com.karrar.fluxapplication.model.RequestCountRTO;
import com.karrar.fluxapplication.service.KinesisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(path = "/api/smaato")
@Slf4j
public class SmaatoController {

    private Sinks.Many sink;
    private final AtomicLong requestsCounter;
    private final KinesisService kinesisService;

    @Autowired
    public SmaatoController(KinesisService kinesisService) {
        this.kinesisService = kinesisService;
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE);
        requestsCounter = new AtomicLong(0);

        Mono.just(1).repeat()
                .delayElements(Duration.ofMinutes(1))
                .concatMap(ignore -> countDistinctIds())
                .doOnNext(kinesisService::streamData)
                .doOnError(e-> log.error(e.getMessage()))
                .subscribe(count -> {
                    log.info("[{}] distinct ids were processed in the last minute", count);
                    sink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE);
                    requestsCounter.set(0);
                });

    }


    @GetMapping(path = "/accept")
    public Mono<String> accept(@RequestParam String id, @RequestParam(required = false) String endpoint) {

        sink.emitNext(id, (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);

        requestsCounter.incrementAndGet();

        return ofNullable(endpoint)
                .map(uri -> WebClient.builder()
                        .baseUrl(uri)
                        .filter(ExchangeFilterFunction.ofResponseProcessor(res -> {
                                    log.info("Response status {}", res.statusCode());
                                    return Mono.just(res);
                                }
                        ))
                        .build()
                        .post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(
                                BodyInserters.fromValue(
                                        new RequestCountRTO(requestsCounter.get())
                                )
                        )
                        .retrieve()
                        .bodyToMono(String.class)
                        .onErrorResume(throwable -> Mono.just("fail"))
                ).orElse(Mono.just("ok"));
    }

    public Mono<Long> countDistinctIds() {
        sink.tryEmitComplete();
        return sink.asFlux().distinct().count();
    }

}
