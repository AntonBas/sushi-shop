package com.sushishop.auth.dto.response;

import com.sushishop.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response")
public record AuthResponse(
        @Schema(description = "User information")
        UserResponse user
) {
}
