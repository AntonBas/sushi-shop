package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Single item in an order")
public record OrderItemRequest(
        @Schema(description = "Product ID", example = "1")
        @NotNull(message = "Product ID is required")
        Long productId,

        @Schema(description = "Quantity must be greater than 0")
        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {
}