package com.sushishop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Order item response")
public record OrderItemResponse(
        @Schema(description = "Product ID", example = "1")
        Long productId,

        @Schema(description = "Product name", example = "Maki")
        String productName,

        @Schema(description = "Quantity", example = "2")
        Integer quantity,

        @Schema(description = "Price per item", example = "250.00")
        BigDecimal price
) {
}
