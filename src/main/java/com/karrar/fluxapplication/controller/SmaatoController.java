package com.karrar.fluxapplication.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(path="/api/smaato")
@Slf4j
public class SmaatoController {

    private Sinks.Many sink;

    public SmaatoController() {
        this.sink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE);
        Mono.just(1).repeat()
                .delayElements(Duration.ofMinutes(1))
                .flatMap(ignore -> countDistinctIds())
                .subscribe(count-> {
                    log.info("[{}] distinct ids were processed in the last minute", count);
                    sink = Sinks.many().multicast().onBackpressureBuffer(Integer.MAX_VALUE);
                });

    }


    @GetMapping(path = "/accept")
    public Mono<String> accept(@RequestParam String id, @RequestParam(required = false) String endpoint) {

        sink.emitNext(id, (signalType, emitResult) -> emitResult == Sinks.EmitResult.FAIL_NON_SERIALIZED);

        return ofNullable(endpoint)
                .map(uri-> WebClient.builder()
                        .baseUrl(uri)
                        .filter(ExchangeFilterFunction.ofResponseProcessor(res ->{
                                    log.info("Response status {}", res.statusCode());
                                    return Mono.just(res);
                                }
                        ))
                        .build()
                        .get()
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
