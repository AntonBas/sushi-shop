package com.sushishop.dto.response;

import com.sushishop.domain.enums.DeliveryMethod;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order response")
public record OrderResponse(
        @Schema(description = "Order ID", example = "1")
        Long id,

        @Schema(description = "Customer name", example = "Anton")
        String customerName,

        @Schema(description = "Contact phone", example = "+380961791111")
        String phone,

        @Schema(description = "Delivery address")
        AddressResponse address,

        @Schema(description = "Delivery method", example = "DELIVERY")
        String deliveryMethod,

        @Schema(description = "Order status", example = "NEW")
        String status,

        @Schema(description = "Total amount", example = "750.00")
        BigDecimal totalAmount,

        @Schema(description = "Order creation time")
        LocalDateTime createdAt,

        @Schema(description = "Order items")
        List<OrderItemResponse> items
) {
}