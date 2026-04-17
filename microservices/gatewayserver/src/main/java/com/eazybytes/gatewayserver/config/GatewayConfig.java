package com.eazybytes.gatewayserver.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder, RedisRateLimiter redisRateLimiter, KeyResolver keyResolver){
        return routeLocatorBuilder.routes()
                .route("accounts_route",  r -> r
                        .path("/accounts/**")
                        .filters(f -> f.rewritePath("/accounts/(?<segment>.*)", "/${segment}")
                                .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
                                        .setFallbackUri("forward:/default")
                                )
                        )
//                        .uri("lb://ACCOUNTS") client side load balancing
                        .uri("http://accounts:40001")

                )



                .route("cards_route", r -> r
                        .path("/cards/**")
                        .filters(f -> f.rewritePath("/cards/(?<segment>.*)", "/${segment}")
//                                .circuitBreaker(config -> config.setName("cardsCircuitBreaker")
//                                        .setFallbackUri("forward:/default")
//                                )
                        )
//                        .uri("lb://CARDS")  client side load balancing
                        .uri("http://cards:40002")
                )

                .route("loans_route", r -> r
                        .path("/loans/**")
                        .filters(f -> f.rewritePath("/loans/(?<segment>.*)", "/${segment}")
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET)
                                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)
                                )
                                .requestRateLimiter(config -> config
                                        .setRateLimiter(redisRateLimiter)
                                        .setKeyResolver(keyResolver)
                                )
                        )
//                        .uri("lb://LOANS")  client side load balancing
                        .uri("http://loans:40003")
                )
                .build();
    }

    //default circuit breaker timeout config

    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                // 1. Configure the Circuit Breaker logic (thresholds, window size, etc.)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .slidingWindowSize(10)
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofSeconds(10))
                        .build())
                // 2. Configure the Time Limiter (Timeout duration)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build())
                .build());
    }


    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1, 1);
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                exchange.getRequest().getHeaders().getFirst("user")
        ).defaultIfEmpty("anonymous");
    }

}
