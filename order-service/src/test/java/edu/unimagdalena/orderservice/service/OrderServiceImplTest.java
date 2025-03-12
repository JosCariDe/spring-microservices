package edu.unimagdalena.orderservice.service;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.repository.OrderRepository;
import edu.unimagdalena.orderservice.service.OrderServiceImpl;
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
public class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

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
        when(orderRepository.findAll()).thenReturn(orders);

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(orders);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WithExistingId_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        Optional<Order> result = orderService.getOrderById(orderId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(order);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrderById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.getOrderById(nonExistingId);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(nonExistingId);
    }

    @Test
    void createOrder_ShouldSaveAndReturnOrder() {
        // Arrange
        Order newOrder = Order.builder()
                .products(Arrays.asList(UUID.randomUUID()))
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build();

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .products(newOrder.getProducts())
                .orderDate(LocalDateTime.now())
                .status(newOrder.getStatus())
                .totalAmount(newOrder.getTotalAmount())
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // Act
        Order result = orderService.createOrder(newOrder);

        // Assert
        assertThat(result).isEqualTo(savedOrder);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_WithExistingId_ShouldUpdateAndReturnOrder() {
        // Arrange
        Order orderToUpdate = Order.builder()
                .status(OrderStatus.SHIPPED)
                .totalAmount(new BigDecimal("150.00"))
                .build();

        Order updatedOrder = Order.builder()
                .id(orderId)
                .products(order.getProducts())
                .orderDate(order.getOrderDate())
                .status(OrderStatus.SHIPPED)
                .totalAmount(new BigDecimal("150.00"))
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // Act
        Optional<Order> result = orderService.updateOrder(orderId, orderToUpdate);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(result.get().getTotalAmount()).isEqualTo(new BigDecimal("150.00"));
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        Order orderToUpdate = Order.builder()
                .status(OrderStatus.SHIPPED)
                .build();

        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.updateOrder(nonExistingId, orderToUpdate);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(nonExistingId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithExistingId_ShouldUpdateStatusAndReturnOrder() {
        // Arrange
        OrderStatus newStatus = OrderStatus.SHIPPED;

        Order updatedOrder = Order.builder()
                .id(orderId)
                .products(order.getProducts())
                .orderDate(order.getOrderDate())
                .status(newStatus)
                .totalAmount(order.getTotalAmount())
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(updatedOrder);

        // Act
        Optional<Order> result = orderService.updateOrderStatus(orderId, newStatus);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(newStatus);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        UUID nonExistingId = UUID.randomUUID();
        OrderStatus newStatus = OrderStatus.SHIPPED;

        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act
        Optional<Order> result = orderService.updateOrderStatus(nonExistingId, newStatus);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findById(nonExistingId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrder_ShouldCallRepositoryDelete() {
        // Act
        orderService.deleteOrder(orderId);

        // Assert
        verify(orderRepository, times(1)).deleteById(orderId);
    }
}

