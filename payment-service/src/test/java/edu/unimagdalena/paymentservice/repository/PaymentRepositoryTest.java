package edu.unimagdalena.paymentservice.repository;

import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void savePayment_ShouldPersistPayment() {
        // Arrange
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("150.00"))
                .build();

        // Act
        Payment savedPayment = paymentRepository.save(payment);

        // Assert
        assertThat(savedPayment.getId()).isNotNull();
        assertThat(savedPayment.getOrderId()).isNotNull();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedPayment.getAmount()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void findByOrderId_WithExistingOrderId_ShouldReturnPayment() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .orderId(orderId)
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.COMPLETED)
                .amount(new BigDecimal("200.00"))
                .build();

        paymentRepository.save(payment);

        // Act
        Optional<Payment> foundPayment = paymentRepository.findByOrderId(orderId);

        // Assert
        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getOrderId()).isEqualTo(orderId);
        assertThat(foundPayment.get().getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
    }

    @Test
    void findByOrderId_WithNonExistingOrderId_ShouldReturnEmpty() {
        // Act
        Optional<Payment> foundPayment = paymentRepository.findByOrderId(UUID.randomUUID());

        // Assert
        assertThat(foundPayment).isEmpty();
    }

    @Test
    void deleteById_ShouldRemovePayment() {
        // Arrange
        Payment payment = Payment.builder()
                .orderId(UUID.randomUUID())
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .amount(new BigDecimal("100.00"))
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Act
        paymentRepository.deleteById(savedPayment.getId());
        Optional<Payment> deletedPayment = paymentRepository.findById(savedPayment.getId());

        // Assert
        assertThat(deletedPayment).isEmpty();
    }
}
