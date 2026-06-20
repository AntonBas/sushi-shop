package com.sushishop.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Product in menu list")
public record ProductListResponse(
        @Schema(description = "Product ID", example = "1")
        Long id,

        @Schema(description = "Product name", example = "Maki")
        String name,

        @Schema(description = "Product price", example = "250.00")
        BigDecimal price,

        @Schema(description = "Product category", example = "ROLL")
        String category,

        @Schema(description = "Image URL", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "Is product available", example = "true")
        boolean available
) {
}
