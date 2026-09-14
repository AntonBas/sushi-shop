package com.sushishop.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OAuth2 one-time exchange code request")
public record OAuth2ExchangeRequest(
        @NotBlank(message = "Code is required")
        @Schema(description = "One-time code issued after OAuth2 login")
        String code
) {
}
