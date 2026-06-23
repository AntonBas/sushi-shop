package com.sushishop.dto.response;

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

        @Schema(description = "Product category", example = "ROLL")
        String category,

        @Schema(description = "Product images", example = "[\"/api/files/abc.jpg\", \"/api/files/def.jpg\"]")
        List<String> images,

        @Schema(description = "Is product available", example = "true")
        boolean available
) {
}