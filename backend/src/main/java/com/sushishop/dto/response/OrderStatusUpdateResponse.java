package com.sushishop.dto.response;

public record OrderStatusUpdateResponse(
        Long orderId,
        String status
) {
}
