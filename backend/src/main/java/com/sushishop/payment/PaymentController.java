package com.sushishop.payment;

import com.sushishop.shared.ratelimit.RateLimit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment endpoints")
public class PaymentController {

    private final CheckoutService checkoutService;
    private final StripeWebhookService stripeWebhookService;

    @RateLimit(key = "user")
    @PostMapping("/order/{orderId}")
    @Operation(summary = "Create Stripe checkout session for order")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> createCheckout(@PathVariable Long orderId,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of("url", checkoutService.createCheckout(orderId, userDetails.getUsername())));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook endpoint")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeWebhookService.handle(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
