package unimagdalena.edu.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unimagdalena.edu.gateway.filters.CorrelationIdFilter;
import unimagdalena.edu.gateway.filters.factory.SampleCookieGatewayFilterFactory;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, SampleCookieGatewayFilterFactory cookieFilter) {
        return builder.routes()
                // Ruta para order-service con Circuit Breaker
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .setName("orderServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://order-service"))

                // Ruta para inventory-service con Circuit Breaker
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .setName("inventoryServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://inventory-service"))

                // Ruta para product-service con Circuit Breaker
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .setName("productServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://product-service"))

                // Ruta para payment-service con Circuit Breaker
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(2)
                                .circuitBreaker(c -> c
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback")))
                        .uri("lb://payment-service"))
                .build();
    }
}