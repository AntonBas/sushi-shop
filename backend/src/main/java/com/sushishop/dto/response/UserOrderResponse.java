package com.sushishop.dto.response;

import com.sushishop.domain.enums.DeliveryMethod;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "User order response")
public record UserOrderResponse(
        @Schema(description = "Order ID", example = "1")
        Long id,

        @Schema(description = "Order status", example = "NEW")
        OrderStatus status,

        @Schema(description = "Payment status", example = "PENDING")
        PaymentStatus paymentStatus,

        @Schema(description = "Delivery method", example = "DELIVERY")
        DeliveryMethod deliveryMethod,

        @Schema(description = "Total amount", example = "750.00")
        BigDecimal totalAmount,

        @Schema(description = "Order creation time")
        LocalDateTime createdAt,

        @Schema(description = "Order items")
        List<OrderItemResponse> items
) {
}