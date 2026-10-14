package com.sushishop.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current user's review status for a product")
public record ReviewStatusResponse(
        @Schema(description = "Whether the current user has already reviewed the product", example = "true")
        boolean reviewed,

        @Schema(description = "Whether the current user has a delivered order with the product", example = "true")
        boolean purchased
) {
}
