package com.sushishop.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Review response")
public record ReviewResponse(
        @Schema(description = "Review ID", example = "1")
        Long id,

        @Schema(description = "User ID", example = "1")
        Long userId,

        @Schema(description = "User name", example = "Anton")
        String userName,

        @Schema(description = "Rating", example = "5")
        Integer rating,

        @Schema(description = "Comment", example = "Very tasty!")
        String comment,

        @Schema(description = "Replies to this review")
        List<ReviewReplyResponse> replies,

        @Schema(description = "Created date")
        LocalDateTime createdAt,

        @Schema(description = "Last update time")
        LocalDateTime updatedAt
) {
}
