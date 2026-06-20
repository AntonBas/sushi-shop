package com.sushishop.dto.request;

import com.sushishop.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Request to update an existing product")
public record UpdateProductRequest(

        @Schema(description = "Product name", example = "Maki")
        String name,

        @Schema(description = "Product description", example = "Updated description")
        String description,

        @Schema(description = "Product price", example = "280.00")
        @Positive(message = "Price must be greater than 0")
        BigDecimal price,

        @Schema(description = "Product category", example = "ROLL")
        Category category,

        @Schema(description = "Image URL", example = "https://example.com/new-image.jpg")
        String imageUrl,

        @Schema(description = "Product availability")
        Boolean isAvailable
) {
}
