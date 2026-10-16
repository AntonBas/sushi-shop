package com.sushishop.admin.dto;

import com.sushishop.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "User as seen in the admin panel")
public record AdminUserResponse(
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

        @Schema(description = "Whether the email is verified", example = "true")
        boolean emailVerified,

        @Schema(description = "Whether the user can sign in with a password", example = "true")
        boolean hasPassword,

        @Schema(description = "Registration time")
        LocalDateTime createdAt,

        @Schema(description = "Number of orders placed", example = "3")
        long ordersCount,

        @Schema(description = "Total amount of delivered orders", example = "1250.00")
        BigDecimal totalSpent
) {
}
