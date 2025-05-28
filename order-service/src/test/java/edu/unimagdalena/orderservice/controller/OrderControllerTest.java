package edu.unimagdalena.orderservice.controller;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.repository.OrderRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class OrderControllerTest {

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
    private OrderRepository orderRepository;

    private WebTestClient webTestClient;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        testOrder = Order.builder()
                .products(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Act & Assert
        webTestClient.get()
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Order.class)
                .value(orders -> {
                    assertThat(orders).hasSize(1);
                    Order returnedOrder = orders.get(0);
                    assertThat(returnedOrder.getId()).isEqualTo(testOrder.getId());
                    assertThat(returnedOrder.getProducts()).isEqualTo(testOrder.getProducts());
                    assertThat(returnedOrder.getStatus()).isEqualTo(testOrder.getStatus());
                    assertThat(returnedOrder.getTotalAmount()).isEqualByComparingTo(testOrder.getTotalAmount());
                    // Para LocalDateTime, comparamos solo la fecha sin considerar nanosegundos
                    assertThat(returnedOrder.getOrderDate().toLocalDate())
                            .isEqualTo(testOrder.getOrderDate().toLocalDate());
                });
    }

    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() {
        // Act & Assert
        webTestClient.get()
                .uri("/{id}", testOrder.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .value(returnedOrder -> {
                    assertThat(returnedOrder.getId()).isEqualTo(testOrder.getId());
                    assertThat(returnedOrder.getProducts()).isEqualTo(testOrder.getProducts());
                    assertThat(returnedOrder.getStatus()).isEqualTo(testOrder.getStatus());
                    assertThat(returnedOrder.getTotalAmount()).isEqualByComparingTo(testOrder.getTotalAmount());
                    assertThat(returnedOrder.getOrderDate().toLocalDate())
                            .isEqualTo(testOrder.getOrderDate().toLocalDate());
                });
    }

    @Test
    void getOrderById_WithNonExistingId_ShouldReturnNotFound() {
        // Act & Assert
        webTestClient.get()
                .uri("/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        // Arrange
        Order newOrder = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        // Act & Assert
        webTestClient.post()
                .bodyValue(newOrder)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Order.class)
                .value(order -> {
                    assertThat(order.getId()).isNotNull();
                    assertThat(order.getProducts()).isEqualTo(newOrder.getProducts());
                    assertThat(order.getStatus()).isEqualTo(newOrder.getStatus());
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(newOrder.getTotalAmount());
                    assertThat(order.getOrderDate()).isNotNull();
                });
    }

    @Test
    void updateOrder_WithExistingId_ShouldReturnUpdatedOrder() {
        // Arrange
        Order orderUpdate = Order.builder()
                .status(OrderStatus.SHIPPED)
                .totalAmount(new BigDecimal("150.00"))
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/{id}", testOrder.getId())
                .bodyValue(orderUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .value(order -> {
                    assertThat(order.getId()).isEqualTo(testOrder.getId());
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
                    assertThat(order.getProducts()).isEqualTo(testOrder.getProducts());
                    assertThat(order.getOrderDate().toLocalDate())
                            .isEqualTo(testOrder.getOrderDate().toLocalDate());
                });
    }

    @Test
    void updateOrder_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        Order orderUpdate = Order.builder()
                .status(OrderStatus.SHIPPED)
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/{id}", UUID.randomUUID())
                .bodyValue(orderUpdate)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateOrderStatus_WithExistingId_ShouldReturnUpdatedOrder() {
        // Act & Assert
        webTestClient.patch()
                .uri("/{id}/status", testOrder.getId())
                .bodyValue(OrderStatus.SHIPPED)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Order.class)
                .value(order -> {
                    assertThat(order.getId()).isEqualTo(testOrder.getId());
                    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
                    assertThat(order.getProducts()).isEqualTo(testOrder.getProducts());
                    assertThat(order.getOrderDate().toLocalDate())
                            .isEqualTo(testOrder.getOrderDate().toLocalDate());
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(testOrder.getTotalAmount());
                });
    }

    @Test
    void updateOrderStatus_WithNonExistingId_ShouldReturnNotFound() {
        // Act & Assert
        webTestClient.patch()
                .uri("/{id}/status", UUID.randomUUID())
                .bodyValue(OrderStatus.SHIPPED)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteOrder_ShouldRemoveOrder() {
        // Act & Assert
        webTestClient.delete()
                .uri("/{id}", testOrder.getId())
                .exchange()
                .expectStatus().isNoContent();

        // Verify order was deleted
        webTestClient.get()
                .uri("/{id}", testOrder.getId())
                .exchange()
                .expectStatus().isNotFound();
    }
}

