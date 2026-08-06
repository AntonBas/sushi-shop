package com.sushishop.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Product image response")
public record ProductImageResponse(
        @Schema(description = "Image ID", example = "1")
        Long id,

        @Schema(description = "Image URL", example = "/api/files/abc.jpg")
        String url
) {
}
