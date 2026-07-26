package com.sushishop.auth.dto.response;

import com.sushishop.user.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response")
public record AuthResponse(
        @Schema(description = "JWT token")
        String token,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType,

        @Schema(description = "User information")
        UserResponse user
) {
    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", user);
    }
}
