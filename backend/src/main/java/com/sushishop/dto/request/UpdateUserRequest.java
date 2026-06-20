package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Change password request")
public record UpdateUserRequest(

        @Schema(description = "Full name", example = "Anton Bas")
        String name,

        @Schema(description = "Phone number", example = "+380961791111")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits, optionally starting with +")
        String phone,

        @Schema(description = "Default delivery address")
        @Valid
        AddressRequest address
) {
}
