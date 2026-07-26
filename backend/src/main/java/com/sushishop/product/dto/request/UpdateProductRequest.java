package com.sushishop.product.dto.request;

import com.sushishop.shared.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request to update an existing product")
public record UpdateProductRequest(
        @Schema(description = "Product name", example = "Maki")
        @Size(max = 50, message = "Name must be less than 50 characters")
        String name,

        @Schema(description = "Product description", example = "Updated description")
        @Size(max = 250, message = "Description must be less than 250 characters")
        String description,

        @Schema(description = "Product price", example = "280.00")
        @Positive(message = "Price must be greater than 0")
        BigDecimal price,

        @Schema(description = "Product category", example = "ROLL")
        Category category,

        @Schema(description = "Product availability")
        Boolean isAvailable,

        @Schema(description = "Product weight", example = "250")
        @Positive(message = "Weight must be greater than 0")
        Integer weight,

        @Schema(description = "Pieces for sets", example = "8")
        @Positive(message = "Pieces must be greater than 0")
        Integer pieces
) {
}