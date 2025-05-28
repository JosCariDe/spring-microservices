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
                // Ruta para order-service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> (org.springframework.cloud.gateway.route.builder.UriSpec) f
                                .stripPrefix(2))
                        .uri("lb://order-service"))
                // Ruta para inventory-service con RequestRateLimiter
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("lb://inventory-service"))
                // Ruta para product-service
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("lb://product-service"))
                // Ruta para payment-service
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(2))
                        .uri("lb://payment-service"))
                // Discovery Server Routes
                // Discovery Server Web UI
                .route("discovery-service", r -> r
                        .path("/eureka/web")
                        .filters(f -> f.setPath("/"))
                        .uri("http://localhost:8861"))

                // Discovery Server Static Resources
                .route("discovery-service-static", r -> r
                        .path("/eureka/**")
                        .uri("http://localhost:8861"))

                // Actuator Routes for Monitoring
                // Inventory Service Actuator
                .route("inventory-service-actuator", r -> r
                        .path("/actuator/inventory/**")
                        .uri("lb://inventory-service/actuator/inventory/**"))

                // Orders Service Actuator
                .route("orders-service-actuator", r -> r
                        .path("/actuator/orders/**")
                        .uri("lb://order-service/actuator/orders/**"))

                // Products Service Actuator
                .route("products-service-actuator", r -> r
                        .path("/actuator/products/**")
                        .uri("lb://product-service/actuator/products/**"))

                .route("payments-service-actuator", r -> r
                        .path("/actuator/payments/**")
                        .uri("lb://payment-service/actuator/products/**"))
                .build();
    }

}
