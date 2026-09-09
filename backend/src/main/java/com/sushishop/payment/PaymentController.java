package com.sushishop.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment endpoints")
public class PaymentController {

    private final StripeService stripeService;
    private final PaymentService paymentService;

    @PostMapping("/order/{orderId}")
    @Operation(summary = "Create Stripe checkout session for order")
    public ResponseEntity<Map<String, String>> createCheckout(@PathVariable Long orderId,
                                                              @RequestParam Long amountInCents,
                                                              @RequestParam String email) {
        var info = stripeService.createCheckoutSession(orderId, amountInCents, email);
        try {
            paymentService.create(orderId, info.id(), BigDecimal.valueOf(amountInCents, 2));
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
        var sessionId = stripeService.getSessionIdFromWebhook(payload, sigHeader);
        if (sessionId != null) {
            paymentService.confirmPayment(sessionId);
        }
        return ResponseEntity.ok().build();
    }
}