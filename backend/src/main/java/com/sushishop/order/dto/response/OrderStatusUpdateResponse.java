package com.sushishop.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order status update sent via WebSocket")
public record OrderStatusUpdateResponse(
        @Schema(description = "Order ID", example = "1")
        Long orderId,

        @Schema(description = "New order status", example = "COOKING")
        String status
) {
}