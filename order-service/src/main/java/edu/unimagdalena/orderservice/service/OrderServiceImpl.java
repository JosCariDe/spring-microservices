package edu.unimagdalena.orderservice.service;

import edu.unimagdalena.orderservice.model.Order;
import edu.unimagdalena.orderservice.model.OrderStatus;
import edu.unimagdalena.orderservice.repository.OrderRepository;
import edu.unimagdalena.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order createOrder(Order order) {
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDateTime.now());
        }
        return orderRepository.save(order);
    }

    @Override
    public Optional<Order> updateOrder(UUID id, Order orderDetails) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    if (orderDetails.getProducts() != null) {
                        existingOrder.setProducts(orderDetails.getProducts());
                    }
                    if (orderDetails.getStatus() != null) {
                        existingOrder.setStatus(orderDetails.getStatus());
                    }
                    if (orderDetails.getTotalAmount() != null) {
                        existingOrder.setTotalAmount(orderDetails.getTotalAmount());
                    }
                    if (orderDetails.getPaymentId() != null) {
                        existingOrder.setPaymentId(orderDetails.getPaymentId());
                    }
                    return orderRepository.save(existingOrder);
                });
    }

    @Override
    public Optional<Order> updateOrderStatus(UUID id, OrderStatus status) {
        return orderRepository.findById(id)
                .map(existingOrder -> {
                    existingOrder.setStatus(status);
                    return orderRepository.save(existingOrder);
                });
    }

    @Override
    public void deleteOrder(UUID id) {
        orderRepository.deleteById(id);
    }
}

