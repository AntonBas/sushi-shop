package com.sushishop.dto;

import com.sushishop.domain.Order;
import com.sushishop.domain.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank String customerName,
        @NotBlank String phone,
        @NotBlank String address,
        @NotEmpty List<OrderItemRequest> items
) {
}
