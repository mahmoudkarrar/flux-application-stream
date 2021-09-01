package com.karrar.fluxapplication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.util.Optional.ofNullable;

@RestController
@RequestMapping(path="/api/smaato")
public class SmaatoController {
    @GetMapping(path = "/accept")
    public Mono<String> sayHallo(@RequestParam String id, @RequestParam(required = false) String endpoint) {

        return ofNullable(endpoint)
                .map(uri-> WebClient.builder()
                        .baseUrl(uri)
                        .filter(ExchangeFilterFunction.ofResponseProcessor(res ->{
                                    System.out.println("Response status "+ res.statusCode());
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
}
