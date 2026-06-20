package com.sushishop.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Delivery address")
public record AddressRequest(
        @Schema(description = "City name", example = "Lviv")
        @NotBlank(message = "City is required")
        String city,

        @Schema(description = "Street name", example = "Zelena")
        @NotBlank(message = "Street is required")
        String street,

        @Schema(description = "House number", example = "204")
        @NotBlank(message = "House number is required")
        String house,

        @Schema(description = "Apartment number", example = "280")
        String apartment,

        @Schema(description = "Delivery instructions", example = "10 floor, code 123")
        String comment
) {
}
