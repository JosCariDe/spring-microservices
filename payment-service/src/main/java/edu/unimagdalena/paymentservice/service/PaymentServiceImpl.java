package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.model.Order;
import edu.unimagdalena.paymentservice.model.OrderStatus;
import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.model.PaymentStatus;
import edu.unimagdalena.paymentservice.repository.PaymentRepository;
import edu.unimagdalena.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    public static final String PAYMENT_CACHE = "payment";


    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Cacheable(value = PAYMENT_CACHE, key = "#id")
    public Optional<Payment> getPaymentById(UUID id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Mono<Payment> getPaymentByOrderId(UUID orderId) {
        return orderServiceClient.getOrderById(orderId)
                .onErrorResume(WebClientResponseException.NotFound.class, e ->
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found for the given ID", e))
                )
                .flatMap(order -> Mono.justOrEmpty(paymentRepository.findById(order.getPaymentId())))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for the given order ID")));
    }

    @Override
    public Payment createPayment(Payment payment) {
        if (payment.getPaymentStatus() == null) {
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        return paymentRepository.save(payment);
    }

    @Override
    @CachePut(value = PAYMENT_CACHE, key = "#id")
    public Mono<Payment> updatePayment(UUID id, Payment paymentDetails) {
        UUID orderId = paymentDetails.getOrderId();

        return orderServiceClient.getOrderById(orderId)
                .flatMap(order -> Mono.justOrEmpty(paymentRepository.findById(id))
                        .publishOn(Schedulers.boundedElastic())
                        .flatMap(existingPayment -> {
                            // Actualizar campos
                            if (paymentDetails.getPaymentMethod() != null) {
                                existingPayment.setPaymentMethod(paymentDetails.getPaymentMethod());
                            }
                            if (paymentDetails.getAmount() != null) {
                                existingPayment.setAmount(paymentDetails.getAmount());
                            }

                            if (paymentDetails.getPaymentStatus() != null) {
                                existingPayment.setPaymentStatus(paymentDetails.getPaymentStatus());

                                // Guardar primero el payment
                                Payment savedPayment = paymentRepository.save(existingPayment);

                                // Luego actualizar el order status reactivamente
                                return updateOrderStatusBasedOnPayment(orderId, paymentDetails.getPaymentStatus())
                                        .then(Mono.just(savedPayment)); // Retornar el payment guardado
                            }

                            return Mono.just(paymentRepository.save(existingPayment));
                        })
                        .switchIfEmpty(Mono.error(new RuntimeException("Payment not found")))
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")));
    }

    private Mono<Void> updateOrderStatusBasedOnPayment(UUID orderId, PaymentStatus paymentStatus) {
        return switch (paymentStatus) {
            case COMPLETED -> orderServiceClient.updateOrderStatus(orderId, OrderStatus.DELIVERED)
                    .then(); // Convertir a Mono<Void>
            case REFUNDED -> orderServiceClient.updateOrderStatus(orderId, OrderStatus.CANCELLED)
                    .then();
            default -> Mono.empty(); // No hacer nada para otros estados
        };
    }

    @Override
    public void deletePayment(UUID id) {
        paymentRepository.deleteById(id);
    }
}
