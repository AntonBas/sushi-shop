package com.sushishop.admin.dto;

import com.sushishop.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Change user role request")
public record ChangeUserRoleRequest(
        @NotNull(message = "Role is required")
        @Schema(description = "New role", example = "COURIER")
        UserRole role
) {
}
