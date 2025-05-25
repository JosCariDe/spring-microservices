package edu.unimagdalena.paymentservice.service;

import edu.unimagdalena.paymentservice.model.Order;
import edu.unimagdalena.paymentservice.model.OrderStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;
import java.util.UUID;

@Service
@FeignClient(name ="order-service" ,url = "http://localhost:8786")
public interface OrderServiceClient {

    @GetMapping("/{id}")
    Optional<Order> getOrderById(@PathVariable("id") UUID id);

    @PatchMapping("/{id}/status")
    Optional<Order> updateOrderStatus(@PathVariable UUID id,@RequestBody OrderStatus status);
}
