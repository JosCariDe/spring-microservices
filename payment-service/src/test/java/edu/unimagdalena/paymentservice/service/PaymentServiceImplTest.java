package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.model.PaymentMethod;
import edu.unimagdalena.paymentservice.model.PaymentStatus;
import edu.unimagdalena.paymentservice.repository.PaymentRepository;
import edu.unimagdalena.paymentservice.service.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private UUID paymentId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        payment = Payment.builder()
                .id(paymentId)
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .amount(new BigDecimal("100.00"))
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        // Arrange
        List<Payment> payments = Arrays.asList(payment, Payment.builder().id(UUID.randomUUID()).build());
        when(paymentRepository.findAll()).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getAllPayments();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(payments);
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void getPaymentById_WithExistingId_ShouldReturnPayment() {
        // Arrange
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // Act
        Optional<Payment> result = paymentService.getPaymentById(paymentId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(payment);
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void getPaymentById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(paymentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Payment> result = paymentService.getPaymentById(nonExistingId);

        // Assert
        assertThat(result).isEmpty();
        verify(paymentRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void getPaymentByOrderId_WithExistingOrderId_ShouldReturnPayment() {
        // Arrange
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // Act
        Optional<Payment> result = paymentService.getPaymentByOrderId(orderId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(payment);
        verify(paymentRepository, times(1)).findByOrderId(orderId);
    }

    @Test
    void getPaymentByOrderId_WithNonExistingOrderId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingOrderId = UUID.randomUUID();
        when(paymentRepository.findByOrderId(nonExistingOrderId)).thenReturn(Optional.empty());

        // Act
        Optional<Payment> result = paymentService.getPaymentByOrderId(nonExistingOrderId);

        // Assert
        assertThat(result).isEmpty();
        verify(paymentRepository, times(1)).findByOrderId(nonExistingOrderId);
    }

    @Test
    void createPayment_WithoutStatusAndDate_ShouldSetDefaultsAndSavePayment() {
        // Arrange
        Payment newPayment = Payment.builder()
                .orderId(UUID.randomUUID())
                .paymentMethod(PaymentMethod.PAYPAL)
                .amount(new BigDecimal("50.00"))
                .build();

        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(newPayment.getOrderId())
                .paymentMethod(newPayment.getPaymentMethod())
                .paymentStatus(PaymentStatus.PENDING)
                .amount(newPayment.getAmount())
                .paymentDate(LocalDateTime.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        Payment result = paymentService.createPayment(newPayment);

        // Assert
        assertThat(result).isEqualTo(savedPayment);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPayment_WithStatusAndDate_ShouldUseProvidedValuesAndSavePayment() {
        // Arrange
        LocalDateTime paymentDate = LocalDateTime.now().minusDays(1);
        Payment newPayment = Payment.builder()
                .orderId(UUID.randomUUID())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .paymentStatus(PaymentStatus.FAILED)
                .amount(new BigDecimal("75.00"))
                .paymentDate(paymentDate)
                .build();

        Payment savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(newPayment.getOrderId())
                .paymentMethod(newPayment.getPaymentMethod())
                .paymentStatus(newPayment.getPaymentStatus())
                .amount(newPayment.getAmount())
                .paymentDate(paymentDate)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // Act
        Payment result = paymentService.createPayment(newPayment);

        // Assert
        assertThat(result).isEqualTo(savedPayment);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getPaymentDate()).isEqualTo(paymentDate);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void updatePayment_WithExistingId_ShouldUpdateAndReturnPayment() {
        // Arrange
        Payment paymentToUpdate = Payment.builder()
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .paymentStatus(PaymentStatus.REFUNDED)
                .amount(new BigDecimal("150.00"))
                .build();

        Payment updatedPayment = Payment.builder()
                .id(paymentId)
                .orderId(payment.getOrderId())
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .paymentStatus(PaymentStatus.REFUNDED)
                .amount(new BigDecimal("150.00"))
                .paymentDate(payment.getPaymentDate())
                .build();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(updatedPayment);

        // Act
        Optional<Payment> result = paymentService.updatePayment(paymentId, paymentToUpdate);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPaymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
        assertThat(result.get().getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(result.get().getAmount()).isEqualTo(new BigDecimal("150.00"));
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void updatePayment_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        Payment paymentToUpdate = Payment.builder()
                .paymentStatus(PaymentStatus.FAILED)
                .build();

        when(paymentRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Payment> result = paymentService.updatePayment(nonExistingId, paymentToUpdate);

        // Assert
        assertThat(result).isEmpty();
        verify(paymentRepository, times(1)).findById(nonExistingId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void deletePayment_ShouldCallRepositoryDelete() {
        // Act
        paymentService.deletePayment(paymentId);

        // Assert
        verify(paymentRepository, times(1)).deleteById(paymentId);
    }
}

