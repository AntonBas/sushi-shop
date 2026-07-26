package com.sushishop.shared.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Delivery address")
public record AddressRequest(
        @Schema(description = "City name", example = "Lviv")
        @NotBlank(message = "City is required")
        @Size(max = 50, message = "City must be less than 50 characters")
        String city,

        @Schema(description = "Street name", example = "Zelena")
        @NotBlank(message = "Street is required")
        @Size(max = 50, message = "Street must be less than 50 characters")
        String street,

        @Schema(description = "House number", example = "204")
        @NotBlank(message = "House number is required")
        @Size(max = 10, message = "House must be less than 10 characters")
        String house,

        @Schema(description = "Apartment number", example = "280")
        @Size(max = 10, message = "Apartment must be less than 10 characters")
        String apartment,

        @Schema(description = "Delivery instructions", example = "10 floor, code 123")
        @Size(max = 200, message = "Comment must be less than 200 characters")
        String comment
) {
}