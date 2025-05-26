package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.model.Order;
import edu.unimagdalena.paymentservice.model.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OrderServiceClient {

    private final WebClient webClient;


    public OrderServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("lb://ORDER-SERVICE").build();
    }

    public Mono<Order> getOrderById(UUID orderId) {
        return webClient.get()
                .uri("/api/orders/{orderId}", orderId)
                .retrieve()
                .bodyToMono(Order.class);
    }

    public Mono<Order> updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        return webClient.patch()
                .uri("/api/orders/{orderId}/status", orderId)
                .bodyValue(newStatus)
                .retrieve()
                .bodyToMono(Order.class);
    }
}
