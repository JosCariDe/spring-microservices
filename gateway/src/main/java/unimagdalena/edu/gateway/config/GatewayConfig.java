package unimagdalena.edu.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("order-service", r -> r
                        .path("/orders/**")
                        .uri("lb://order-service"))
                .route("inventory-service", r -> r
                        .path("/inventory/**")
                        .uri("lb://inventory-service"))
                .route("product-service", r -> r
                        .path("/products/**")
                        .uri("lb://product-service"))
                .route("payment-service", r -> r
                        .path("/payments/**")
                        .uri("lb://payment-service"))
                .build();
    }
} 