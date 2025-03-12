package edu.unimagdalena.orderservice.service;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(UUID id);
    Order createOrder(Order order);
    Optional<Order> updateOrder(UUID id, Order orderDetails);
    Optional<Order> updateOrderStatus(UUID id, OrderStatus status);
    void deleteOrder(UUID id);
}