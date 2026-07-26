package com.sushishop.shared.address;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Delivery address")
public record AddressResponse(
        @Schema(description = "City name", example = "Lviv")
        String city,

        @Schema(description = "Street name", example = "Zelena")
        String street,

        @Schema(description = "House number", example = "204")
        String house,

        @Schema(description = "Apartment number", example = "280")
        String apartment,

        @Schema(description = "Delivery instructions", example = "10 floor, code 123")
        String comment
) {
}
