package com.eazybytes.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder){
        return routeLocatorBuilder.routes()
                .route("accounts_route", r -> r
                        .path("/accounts/**")
                        .filters(f -> f.rewritePath("/accounts/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/default")
                                )
                        )
                        .uri("lb://ACCOUNTS"))
                .route("cards_route", r -> r
                        .path("/cards/**")
                        .filters(f -> f.rewritePath("/cards/(?<segment>.*)", "/${segment}")
//                                .circuitBreaker(config -> config.setName("cardsCircuitBreaker")
//                                        .setFallbackUri("forward:/default")
//                                )
                        )
                        .uri("lb://CARDS"))
                .route("loans_route", r -> r
                        .path("/loans/**")
                        .filters(f -> f.rewritePath("/loans/(?<segment>.*)", "/${segment}")
//                                .circuitBreaker(config -> config.setName("loansCircuitBreaker")
//                                        .setFallbackUri("forward:/default")
//                                )
                        )
                        .uri("lb://LOANS"))
                .build();
    }
}
