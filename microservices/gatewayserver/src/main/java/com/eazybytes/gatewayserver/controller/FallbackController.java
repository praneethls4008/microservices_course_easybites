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

        // 1. Extract Correlation ID from headers for traceability
        String correlationId = exchange.getRequest().getHeaders().getFirst("eazybank-correlation-id");

        // 2. Identify which path actually failed
        String originalUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR).toString();

        Throwable error = exchange.getAttribute(
                ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR
        );

        // 3. Log with structured context
        if (error != null) {
            log.error("Circuit Breaker Fallback | CorrelationID: {} | Path: {} | Reason: {} | Message: {}",
                    correlationId, originalUri, error.getClass().getSimpleName(), error.getMessage());
        } else {
            log.warn("Fallback triggered with no specific exception | Path: {}", originalUri);
        }

        return Mono.just("An error occurred. Please try after some time or contact support team!");
    }
}