package com.sushishop.product.dto.response;

import com.sushishop.product.Category;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Product in menu list")
public record ProductListResponse(
        @Schema(description = "Product ID", example = "1")
        Long id,

        @Schema(description = "Product slug", example = "california-roll")
        String slug,

        @Schema(description = "Product name", example = "Maki")
        String name,

        @Schema(description = "Product price", example = "250.00")
        BigDecimal price,

        @Schema(description = "Discounted price", example = "200.00")
        BigDecimal discountedPrice,

        @Schema(description = "Average rating", example = "4.5")
        Double averageRating,

        @Schema(description = "Product category", example = "ROLL")
        Category category,

        @Schema(description = "Main product image", example = "/api/files/abc.jpg")
        String mainImage,

        @Schema(description = "Is product available", example = "true")
        boolean available,

        @Schema(description = "Product weight", example = "250g")
        Integer weight,

        @Schema(description = "Pieces for sets", example = "8")
        Integer pieces
) {
}