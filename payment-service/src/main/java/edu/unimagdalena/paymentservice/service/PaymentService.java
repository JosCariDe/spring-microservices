package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.model.PaymentStatus;
import edu.unimagdalena.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {
    List<Payment> getAllPayments();
    Optional<Payment> getPaymentById(UUID id);
    Mono<Payment> getPaymentByOrderId(UUID orderId);
    Payment createPayment(Payment payment);
    Mono<Payment> updatePayment(UUID id, Payment paymentDetails);
    void deletePayment(UUID id);
}
