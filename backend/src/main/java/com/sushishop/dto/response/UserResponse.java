package com.sushishop.dto.response;

import com.sushishop.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User response")
public record UserResponse(
        @Schema(description = "User ID", example = "1")
        Long id,

        @Schema(description = "Full name", example = "Anton Bas")
        String name,

        @Schema(description = "Email address", example = "user@example.com")
        String email,

        @Schema(description = "Phone number", example = "+380961791111")
        String phone,

        @Schema(description = "User role", example = "CUSTOMER")
        UserRole userRole,

        @Schema(description = "Default delivery address")
        AddressResponse address
) {
}
