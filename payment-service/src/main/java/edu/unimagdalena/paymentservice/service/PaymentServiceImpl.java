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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    public Optional<Payment> getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
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
    public Optional<Payment> updatePayment(UUID id, Payment paymentDetails) {

        UUID idORder = paymentDetails.getOrderId();
        Order order = orderServiceClient.getOrderById(idORder).get();

        return paymentRepository.findById(id)
                .map(existingPayment -> {
                    if (paymentDetails.getPaymentMethod() != null) {
                        existingPayment.setPaymentMethod(paymentDetails.getPaymentMethod());
                    }
                    if (paymentDetails.getPaymentStatus() != null) {
                        existingPayment.setPaymentStatus(paymentDetails.getPaymentStatus());
                        switch (paymentDetails.getPaymentStatus()) {
                            case COMPLETED:
                                orderServiceClient.updateOrderStatus(idORder, OrderStatus.DELIVERED);
                                break;
                            case REFUNDED:
                                orderServiceClient.updateOrderStatus(idORder, OrderStatus.CANCELLED);
                        }
                    }
                    if (paymentDetails.getAmount() != null) {
                        existingPayment.setAmount(paymentDetails.getAmount());
                    }
                    return paymentRepository.save(existingPayment);
                });
    }

    @Override
    public void deletePayment(UUID id) {
        paymentRepository.deleteById(id);
    }
}

