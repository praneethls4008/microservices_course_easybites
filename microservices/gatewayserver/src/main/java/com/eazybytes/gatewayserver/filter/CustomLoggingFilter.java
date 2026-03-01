package com.eazybytes.gatewayserver.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomLoggingFilter implements GlobalFilter, Ordered {
     @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            System.out.println("Request Path: " + exchange.getRequest().getPath());
            return chain.filter(exchange);
        }

        @Override
        public int getOrder() {
            return -1;
        }
}
