package edu.unimagdalena.orderservice.controller;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private Order order;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        order = Order.builder()
                .id(orderId)
                .products(Arrays.asList(UUID.randomUUID(), UUID.randomUUID()))
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        List<Order> orders = Arrays.asList(order, Order.builder().id(UUID.randomUUID()).build());
        when(orderService.getAllOrders()).thenReturn(orders);

        // Act & Assert
        StepVerifier.create(orderController.getAllOrders())
                .expectNextCount(2)
                .verifyComplete();

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() {
        // Arrange
        when(orderService.getOrderById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        StepVerifier.create(orderController.getOrderById(orderId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().equals(order)
                )
                .verifyComplete();

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    void getOrderById_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        when(orderService.getOrderById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(orderController.getOrderById(orderId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.NOT_FOUND
                )
                .verifyComplete();

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        // Arrange
        Order orderToCreate = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        when(orderService.createOrder(any(Order.class))).thenReturn(order);

        // Act & Assert
        StepVerifier.create(orderController.createOrder(orderToCreate))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.CREATED &&
                                responseEntity.getBody().equals(order)
                )
                .verifyComplete();

        verify(orderService, times(1)).createOrder(any(Order.class));
    }

    @Test
    void updateOrder_WithExistingId_ShouldReturnUpdatedOrder() {
        // Arrange
        Order orderToUpdate = Order.builder()
                .status(OrderStatus.SHIPPED)
                .totalAmount(new BigDecimal("150.00"))
                .build();

        when(orderService.updateOrder(eq(orderId), any(Order.class))).thenReturn(Optional.of(order));

        // Act & Assert
        StepVerifier.create(orderController.updateOrder(orderId, orderToUpdate))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().equals(order)
                )
                .verifyComplete();

        verify(orderService, times(1)).updateOrder(eq(orderId), any(Order.class));
    }

    @Test
    void updateOrder_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        Order orderToUpdate = Order.builder()
                .status(OrderStatus.SHIPPED)
                .build();

        when(orderService.updateOrder(eq(orderId), any(Order.class))).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(orderController.updateOrder(orderId, orderToUpdate))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.NOT_FOUND
                )
                .verifyComplete();

        verify(orderService, times(1)).updateOrder(eq(orderId), any(Order.class));
    }

    @Test
    void updateOrderStatus_WithExistingId_ShouldReturnUpdatedOrder() {
        // Arrange
        OrderStatus newStatus = OrderStatus.SHIPPED;

        when(orderService.updateOrderStatus(eq(orderId), eq(newStatus))).thenReturn(Optional.of(order));

        // Act & Assert
        StepVerifier.create(orderController.updateOrderStatus(orderId, newStatus))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.OK &&
                                responseEntity.getBody().equals(order)
                )
                .verifyComplete();

        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(newStatus));
    }

    @Test
    void updateOrderStatus_WithNonExistingId_ShouldReturnNotFound() {
        // Arrange
        OrderStatus newStatus = OrderStatus.SHIPPED;

        when(orderService.updateOrderStatus(eq(orderId), eq(newStatus))).thenReturn(Optional.empty());

        // Act & Assert
        StepVerifier.create(orderController.updateOrderStatus(orderId, newStatus))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.NOT_FOUND
                )
                .verifyComplete();

        verify(orderService, times(1)).updateOrderStatus(eq(orderId), eq(newStatus));
    }

    @Test
    void deleteOrder_ShouldReturnNoContent() {
        // Act & Assert
        StepVerifier.create(orderController.deleteOrder(orderId))
                .expectNextMatches(responseEntity ->
                        responseEntity.getStatusCode() == HttpStatus.NO_CONTENT
                )
                .verifyComplete();

        verify(orderService, times(1)).deleteOrder(orderId);
    }
}

