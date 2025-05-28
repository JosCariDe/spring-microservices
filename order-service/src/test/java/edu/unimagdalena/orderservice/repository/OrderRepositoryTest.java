package edu.unimagdalena.orderservice.repository;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderRepositoryTest {

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
    private OrderRepository orderRepository;

    @Test
    void saveOrder_ShouldPersistOrder() {
        // Arrange
        Order order = Order.builder()
                .products(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        // Act
        Order savedOrder = orderRepository.save(order);

        // Assert
        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getProducts()).hasSize(2);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(savedOrder.getTotalAmount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void findById_WithExistingId_ShouldReturnOrder() {
        // Arrange
        Order order = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        Order savedOrder = orderRepository.save(order);

        // Act
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        // Assert
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<Order> foundOrder = orderRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(foundOrder).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Arrange
        orderRepository.deleteAll(); // Ensure clean state

        Order order1 = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        Order order2 = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PROCESSING)
                .totalAmount(new BigDecimal("75.00"))
                .build();

        orderRepository.saveAll(Arrays.asList(order1, order2));

        // Act
        List<Order> orders = orderRepository.findAll();

        // Assert
        assertThat(orders).hasSize(2);
    }

    @Test
    void deleteById_ShouldRemoveOrder() {
        // Arrange
        Order order = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        Order savedOrder = orderRepository.save(order);

        // Act
        orderRepository.deleteById(savedOrder.getId());
        Optional<Order> deletedOrder = orderRepository.findById(savedOrder.getId());

        // Assert
        assertThat(deletedOrder).isEmpty();
    }
}

