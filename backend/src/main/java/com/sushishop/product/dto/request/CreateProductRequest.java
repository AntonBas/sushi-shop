package com.sushishop.product.dto.request;

import com.sushishop.product.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Request to create a new product")
public record CreateProductRequest(
        @Schema(description = "Product name", example = "Maki")
        @NotBlank(message = "Product name is required")
        @Size(max = 50, message = "Name must be less than 50 characters")
        String name,

        @Schema(description = "Product description", example = "Classic salmon roll")
        @Size(max = 500, message = "Description must be less than 500 characters")
        String description,

        @Schema(description = "Product price", example = "250.00")
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 2 decimal places")
        BigDecimal price,

        @Schema(description = "Product category", example = "ROLL")
        @NotNull(message = "Category is required")
        Category category,

        @Schema(description = "Product weight", example = "250")
        @NotNull(message = "Weight is required")
        @Positive(message = "Weight must be greater than 0")
        @Max(value = 5000, message = "Weight must not exceed 5000 grams")
        Integer weight,

        @Schema(description = "Pieces for sets", example = "8")
        @Positive(message = "Pieces must be greater than 0")
        @Max(value = 200, message = "Pieces must not exceed 200")
        Integer pieces
) {
}