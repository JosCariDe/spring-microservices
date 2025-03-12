package edu.unimagdalena.paymentservice.controller;

import edu.unimagdalena.paymentservice.model.Payment;
import edu.unimagdalena.paymentservice.model.PaymentMethod;
import edu.unimagdalena.paymentservice.model.PaymentStatus;
import edu.unimagdalena.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class PaymentControllerTest {

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

    @LocalServerPort
    private int port;

    @Autowired
    private PaymentRepository paymentRepository;

    private WebTestClient webTestClient;
    private Payment testPayment;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        orderId = UUID.randomUUID();
        testPayment = Payment.builder()
                .orderId(orderId)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .amount(new BigDecimal("100.00"))
                .paymentDate(LocalDateTime.now())
                .build();

        testPayment = paymentRepository.save(testPayment);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
    }

    @Test
    void getAllPayments_ShouldReturnAllPayments() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/payments")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Payment.class)
                .value(payments -> {
                    assertThat(payments).hasSize(1);
                    Payment returnedPayment = payments.get(0);
                    assertThat(returnedPayment.getId()).isEqualTo(testPayment.getId());
                    assertThat(returnedPayment.getOrderId()).isEqualTo(testPayment.getOrderId());
                    assertThat(returnedPayment.getPaymentMethod()).isEqualTo(testPayment.getPaymentMethod());
                    assertThat(returnedPayment.getPaymentStatus()).isEqualTo(testPayment.getPaymentStatus());
                    assertThat(returnedPayment.getAmount()).isEqualByComparingTo(testPayment.getAmount());
                    assertThat(returnedPayment.getPaymentDate().toLocalDate())
                            .isEqualTo(testPayment.getPaymentDate().toLocalDate());
                });
    }

    @Test
    void getPaymentById_WithExistingId_ShouldReturnPayment() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/payments/{id}", testPayment.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Payment.class)
                .value(returnedPayment -> {
                    assertThat(returnedPayment.getId()).isEqualTo(testPayment.getId());
                    assertThat(returnedPayment.getOrderId()).isEqualTo(testPayment.getOrderId());
                    assertThat(returnedPayment.getPaymentMethod()).isEqualTo(testPayment.getPaymentMethod());
                    assertThat(returnedPayment.getPaymentStatus()).isEqualTo(testPayment.getPaymentStatus());
                    assertThat(returnedPayment.getAmount()).isEqualByComparingTo(testPayment.getAmount());
                    assertThat(returnedPayment.getPaymentDate().toLocalDate())
                            .isEqualTo(testPayment.getPaymentDate().toLocalDate());
                });
    }

    @Test
    void getPaymentById_WithNonExistingId_ShouldReturnNotFound() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/payments/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getPaymentByOrderId_WithExistingOrderId_ShouldReturnPayment() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/payments/order/{orderId}", orderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Payment.class)
                .value(returnedPayment -> {
                    assertThat(returnedPayment.getId()).isEqualTo(testPayment.getId());
                    assertThat(returnedPayment.getOrderId()).isEqualTo(testPayment.getOrderId());
                    assertThat(returnedPayment.getPaymentMethod()).isEqualTo(testPayment.getPaymentMethod());
                    assertThat(returnedPayment.getPaymentStatus()).isEqualTo(testPayment.getPaymentStatus());
                    assertThat(returnedPayment.getAmount()).isEqualByComparingTo(testPayment.getAmount());
                });
    }

    @Test
    void getPaymentByOrderId_WithNonExistingOrderId_ShouldReturnNotFound() {
        // Act & Assert
        webTestClient.get()
                .uri("/api/payments/order/{orderId}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createPayment_ShouldReturnCreatedPayment() {
        // Arrange
        Payment newPayment = Payment.builder()
                .orderId(UUID.randomUUID())
                .paymentMethod(PaymentMethod.PAYPAL)
                .amount(new BigDecimal("50.00"))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/api/payments")
                .bodyValue(newPayment)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Payment.class)
                .value(payment -> {
                    assertThat(payment.getId()).isNotNull();
                    assertThat(payment.getOrderId()).isEqualTo(newPayment.getOrderId());
                    assertThat(payment.getPaymentMethod()).isEqualTo(newPayment.getPaymentMethod());
                    assertThat(payment.getAmount()).isEqualByComparingTo(newPayment.getAmount());
                    assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
                    assertThat(payment.getPaymentDate()).isNotNull();
                });
    }

    @Test
    void updatePayment_WithExistingId_ShouldReturnUpdatedPayment() {
        // Arrange
        Payment paymentUpdate = Payment.builder()
                .paymentMethod(PaymentMethod.DEBIT_CARD)
                .paymentStatus(PaymentStatus.REFUNDED)
                .amount(new BigDecimal("150.00"))
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/payments/{id}", testPayment.getId())
                .bodyValue(paymentUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Payment.class)
                .value(payment -> {
                    assertThat(payment.getId()).isEqualTo(testPayment.getId());
                    assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.DEBIT_CARD);
                    assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
                    assertThat(payment.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
                    assertThat(payment.getOrderId()).isEqualTo(testPayment.getOrderId());
                    assertThat(payment.getPaymentDate().toLocalDate())
                            .isEqualTo(testPayment.getPaymentDate().toLocalDate());
                });
    }

    @Test
    void updatePayment_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        Payment paymentUpdate = Payment.builder()
                .paymentStatus(PaymentStatus.FAILED)
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/payments/{id}", UUID.randomUUID())
                .bodyValue(paymentUpdate)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deletePayment_ShouldRemovePayment() {
        // Act & Assert
        webTestClient.delete()
                .uri("/api/payments/{id}", testPayment.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify payment was deleted
        webTestClient.get()
                .uri("/api/payments/{id}", testPayment.getId())
                .exchange()
                .expectStatus().isNotFound();
    }
}
