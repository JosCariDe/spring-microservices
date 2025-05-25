package edu.unimagdalena.paymentservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cloud.openfeign.FeignClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Order {
    private UUID id;

    private List<UUID> products;

    private LocalDateTime orderDate;

    private OrderStatus status;

    private BigDecimal totalAmount;

    private UUID paymentId;
}
