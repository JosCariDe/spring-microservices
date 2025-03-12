package edu.unimagdalena.orderservice.controller;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<ResponseEntity<List<Order>>> getAllOrders() {
        return Mono.just(ResponseEntity.ok(orderService.getAllOrders()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Order>> getOrderById(@PathVariable UUID id) {
        return Mono.just(orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @PostMapping
    public Mono<ResponseEntity<Order>> createOrder(@RequestBody Order order) {
        return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(order)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Order>> updateOrder(@PathVariable UUID id, @RequestBody Order order) {
        return Mono.just(orderService.updateOrder(id, order)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @PatchMapping("/{id}/status")
    public Mono<ResponseEntity<Order>> updateOrderStatus(@PathVariable UUID id, @RequestBody OrderStatus status) {
        return Mono.just(orderService.updateOrderStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable UUID id) {
        orderService.deleteOrder(id);
        return Mono.just(ResponseEntity.noContent().build());
    }
}

