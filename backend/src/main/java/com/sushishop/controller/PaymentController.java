package com.sushishop.controller;

import com.sushishop.service.PaymentService;
import com.sushishop.service.StripeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;
    private final PaymentService paymentService;

    @PostMapping("/order/{orderId}")
    public ResponseEntity<Map<String, String>> createCheckout(@PathVariable Long orderId) throws Exception {
        var url = stripeService.createCheckoutSession(orderId, 50000L, "customer@example.com");
        paymentService.create(orderId, url, null);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String sigHeader) {
        paymentService.confirmPayment("test");
        return ResponseEntity.ok().build();
    }
}
