package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.model.PaymentStatus;
import edu.unimagdalena.paymentservice.repository.PaymentRepository;
import edu.unimagdalena.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
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
    public Optional<Payment> updatePayment(UUID id, Payment paymentDetails) {
        return paymentRepository.findById(id)
                .map(existingPayment -> {
                    if (paymentDetails.getPaymentMethod() != null) {
                        existingPayment.setPaymentMethod(paymentDetails.getPaymentMethod());
                    }
                    if (paymentDetails.getPaymentStatus() != null) {
                        existingPayment.setPaymentStatus(paymentDetails.getPaymentStatus());
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

