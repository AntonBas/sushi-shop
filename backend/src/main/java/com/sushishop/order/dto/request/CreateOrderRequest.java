package com.sushishop.order.dto.request;

import com.sushishop.shared.enums.DeliveryMethod;
import com.sushishop.shared.address.AddressRequest;
import com.sushishop.shared.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to create a new order")
public record CreateOrderRequest(
        @Schema(description = "Recipient name", example = "Anton")
        @NotBlank(message = "Customer name is required")
        @Size(max = 50, message = "Name must be less than 50 characters")
        String customerName,

        @Schema(description = "Contact phone", example = "+380961791111")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10-15 digits")
        String phone,

        @Schema(description = "Payment method", example = "ONLINE")
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Schema(description = "Delivery method", example = "DELIVERY")
        @NotNull(message = "Delivery method is required")
        DeliveryMethod deliveryMethod,

        @Schema(description = "Delivery address")
        @Valid
        AddressRequest address,

        @Schema(description = "List of products to order")
        @Valid
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {
}