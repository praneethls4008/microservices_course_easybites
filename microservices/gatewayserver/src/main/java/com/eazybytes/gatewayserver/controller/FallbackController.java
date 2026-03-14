package com.eazybytes.gatewayserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ServerWebExchange;

@RestController
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @RequestMapping("/default")
    public Mono<String> defaultFallback(ServerWebExchange exchange) {

        Throwable error = exchange.getAttribute(
                ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR
        );

        if (error != null) {
            log.error("Fallback triggered due to: {}", error.getClass().getName(), error);
        }

        return Mono.just("An error occurred. Please try after some time or contact support team!");
    }
}