package com.sushishop.product.dto.request;

import com.sushishop.product.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request to update an existing product")
public record UpdateProductRequest(
        @Schema(description = "Product name", example = "Maki")
        @Size(max = 50, message = "Name must be less than 50 characters")
        String name,

        @Schema(description = "Product description", example = "Updated description")
        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @Schema(description = "Product price", example = "280.00")
        @Positive(message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
        BigDecimal price,

        @Schema(description = "Product category", example = "ROLL")
        Category category,

        @Schema(description = "Product weight", example = "250")
        @Positive(message = "Weight must be greater than 0")
        Integer weight,

        @Schema(description = "Pieces for sets", example = "8")
        @Positive(message = "Pieces must be greater than 0")
        Integer pieces
) {
}