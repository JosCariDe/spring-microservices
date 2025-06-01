package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.controller.PaymentController;
import edu.unimagdalena.paymentservice.model.Order;
import edu.unimagdalena.paymentservice.model.OrderStatus;
import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceClient {

    private final Logger logger = LoggerFactory.getLogger(OrderServiceClient.class);

    private final WebClient webClient;
    private final CurrentTraceContext currentTraceContext;


    public OrderServiceClient(WebClient.Builder webClientBuilder, CurrentTraceContext currentTraceContext) {
        this.webClient = webClientBuilder.baseUrl("lb://ORDER-SERVICE").build();
        this.currentTraceContext = currentTraceContext;
    }

    public Mono<Order> getOrderById(UUID orderId) {
        logger.info("OrderServiceClient instantiated");
        return webClient.get()
                .uri("/{orderId}", orderId)
                .retrieve()
                .bodyToMono(Order.class);
    }

    public Mono<Order> updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        return webClient.patch()
                .uri("/{orderId}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("\"" + newStatus.name() + "\"")
                .retrieve()
                .bodyToMono(Order.class);
    }
}
