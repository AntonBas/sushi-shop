package com.sushishop.dto.response;

import com.sushishop.domain.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Product response")
public record ProductResponse(
        @Schema(description = "Product ID", example = "1")
        Long id,

        @Schema(description = "Product name", example = "Maki")
        String name,

        @Schema(description = "Product description", example = "Classic salmon roll")
        String description,

        @Schema(description = "Product price", example = "250.00")
        BigDecimal price,

        @Schema(description = "Discounted price", example = "200.00")
        BigDecimal discountedPrice,

        @Schema(description = "Discount percentage", example = "20")
        BigDecimal discountPercent,

        @Schema(description = "Active promotion title", example = "Weekend Sale")
        String promotionTitle,

        @Schema(description = "Product category", example = "ROLL")
        Category category,

        @Schema(description = "Product images", example = "[\"/api/files/abc.jpg\"]")
        List<String> images,

        @Schema(description = "Number of reviews", example = "21")
        Integer reviewCount,

        @Schema(description = "Average rating", example = "4.5")
        Double averageRating,

        @Schema(description = "Is product available", example = "true")
        boolean available,

        @Schema(description = "Product weight", example = "250g")
        String weight,

        @Schema(description = "Pieces for sets", example = "8")
        Integer pieces
) {
}