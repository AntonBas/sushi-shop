package com.sushishop.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Review reply response")
public record ReviewReplyResponse(
        @Schema(description = "Reply ID", example = "1")
        Long id,

        @Schema(description = "Reply message", example = "Thank you for your feedback!")
        String message,

        @Schema(description = "Author name", example = "Admin")
        String authorName,

        @Schema(description = "Creation time")
        LocalDateTime createdAt
) {
}
