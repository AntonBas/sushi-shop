package com.sushishop.order.dto.response;

import com.sushishop.order.DeliveryMethod;
import com.sushishop.order.OrderStatus;
import com.sushishop.order.PaymentMethod;
import com.sushishop.shared.address.AddressResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order response (admin)")
public record OrderResponse(
        @Schema(description = "Order ID", example = "1")
        Long id,

        @Schema(description = "Customer name", example = "Anton")
        String customerName,

        @Schema(description = "User email", example = "user@example.com")
        String userEmail,

        @Schema(description = "Contact phone", example = "+380961791111")
        String phone,

        @Schema(description = "Delivery address")
        AddressResponse address,

        @Schema(description = "Delivery method", example = "DELIVERY")
        DeliveryMethod deliveryMethod,

        @Schema(description = "Payment method", example = "ONLINE")
        PaymentMethod paymentMethod,

        @Schema(description = "Payment status", example = "PAID")
        String paymentStatus,

        @Schema(description = "Order status", example = "NEW")
        OrderStatus status,

        @Schema(description = "Total amount", example = "750.00")
        BigDecimal totalAmount,

        @Schema(description = "Order creation time")
        LocalDateTime createdAt,

        @Schema(description = "Order items")
        List<OrderItemResponse> items
) {
}