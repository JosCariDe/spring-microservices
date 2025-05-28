package edu.unimagdalena.paymentservice.controller;

import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public Mono<ResponseEntity<List<Payment>>> getAllPayments() {
        return Mono.just(ResponseEntity.ok(paymentService.getAllPayments()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Payment>> getPaymentById(@PathVariable UUID id) {
        return Mono.just(paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @GetMapping("/order/{orderId}")
    public Mono<ResponseEntity<Payment>> getPaymentByOrderId(@PathVariable UUID orderId) {
        return Mono.just(paymentService.getPaymentByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @PostMapping
    public Mono<ResponseEntity<Payment>> createPayment(@RequestBody Payment payment) {
        return Mono.just(ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(payment)));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Payment>> updatePayment(@PathVariable UUID id, @RequestBody Payment payment) {
        return Mono.just(paymentService.updatePayment(id, payment)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePayment(@PathVariable UUID id) {
        paymentService.deletePayment(id);
        return Mono.just(ResponseEntity.noContent().build());
    }
}

