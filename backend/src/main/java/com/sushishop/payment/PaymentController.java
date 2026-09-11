package com.sushishop.payment;

import com.sushishop.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment endpoints")
public class PaymentController {

    private final StripeService stripeService;
    private final PaymentService paymentService;
    private final WebhookIdempotencyService webhookIdempotencyService;
    private final OrderService orderService;

    @PostMapping("/order/{orderId}")
    @Operation(summary = "Create Stripe checkout session for order")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Map<String, String>> createCheckout(@PathVariable Long orderId,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        var order = orderService.getOwnedOrder(orderId, userDetails.getUsername(), false);
        var amountInCents = order.getTotalAmount().movePointRight(2).longValueExact();
        var info = stripeService.createCheckoutSession(orderId, amountInCents, userDetails.getUsername());
        try {
            paymentService.create(orderId, info.id(), order.getTotalAmount());
        } catch (RuntimeException e) {
            stripeService.expireCheckoutSession(info.id());
            throw e;
        }
        log.info("Checkout session created for order: {}", orderId);
        return ResponseEntity.ok(Map.of("url", info.url()));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook endpoint")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String sigHeader) {
        var event = stripeService.parseCheckoutCompletedEvent(payload, sigHeader);

        if (!webhookIdempotencyService.markProcessed(event.eventId())) {
            log.info("Duplicate Stripe webhook event ignored: {}", event.eventId());
            return ResponseEntity.ok().build();
        }

        try {
            if (event.sessionId() != null) {
                paymentService.confirmPayment(event.sessionId());
            }
        } catch (RuntimeException e) {
            webhookIdempotencyService.unmark(event.eventId());
            throw e;
        }
        return ResponseEntity.ok().build();
    }
}