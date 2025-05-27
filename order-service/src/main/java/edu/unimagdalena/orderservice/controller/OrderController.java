package edu.unimagdalena.orderservice.controller;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RefreshScope
@RestController
@RequiredArgsConstructor
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;


    @GetMapping
    public Flux<Order> getAllOrders() {

        logger.info("Ingresando al metodo del controller ProductController::list");

        return Flux.fromIterable(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Order>> getOrderById(@PathVariable UUID id) throws InterruptedException {

        logger.info("Obteniendo odrder con id {}", id);
        if (id.equals(UUID.fromString("550e8400-e29b-41d4-a716-446655440010"))){
            throw new IllegalStateException("El id del controller no se puede encontrar!!");
        }
        if (id.equals(UUID.fromString("550e8400-e29b-41d4-a716-44665544000"))){
            TimeUnit.SECONDS.sleep(5L);
        }

        return Mono.justOrEmpty(orderService.getOrderById(id))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(@RequestBody Order order) {
        return Mono.just(orderService.createOrder(order))
                .map(savedOrder -> ResponseEntity.status(HttpStatus.CREATED).body(savedOrder));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Order>> updateOrder(@PathVariable UUID id, @RequestBody Order order) {
        return Mono.justOrEmpty(orderService.updateOrder(id, order))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<Order>> updateOrderStatus(@PathVariable UUID id, @RequestBody OrderStatus status) {
        return Mono.justOrEmpty(orderService.updateOrderStatus(id, status))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return Mono.just(ResponseEntity.noContent().build());
    }

}

