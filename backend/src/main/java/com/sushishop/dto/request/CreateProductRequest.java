package com.sushishop.dto.request;

import com.sushishop.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Request to create a new product")
public record CreateProductRequest(
        @Schema(description = "Product name", example = "Maki")
        @NotBlank(message = "Product name is required")
        String name,

        @Schema(description = "Product description", example = "Classic salmon roll")
        String description,

        @Schema(description = "Product price", example = "250.00")
        @NotNull(message = "Price is required")
        @Positive(message = "Price must be greater than 0")
        BigDecimal price,

        @Schema(description = "Product category", example = "ROLL")
        @NotNull(message = "Category is required")
        Category category,

        @Schema(description = "Image URL", example = "https://example.com/image.jpg")
        String imageUrl
) {
}
