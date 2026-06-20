package com.sushishop.dto.request;

import com.sushishop.domain.enums.DeliveryMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request to create a new order")
public record CreateOrderRequest(
        @Schema(description = "Recipient name", example = "Anton")
        @NotBlank(message = "Customer name is required")
        String customerName,

        @Schema(description = "Contact phone", example = "+380961791111")
        @NotBlank(message = "Phone number is required")
        String phone,

        @Schema(description = "Delivery method", example = "DELIVERY")
        @NotNull(message = "Delivery method is required")
        DeliveryMethod deliveryMethod,

        @Schema(description = "Delivery address")
        @Valid
        AddressRequest address,

        @Schema(description = "List of products to order")
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {
}
