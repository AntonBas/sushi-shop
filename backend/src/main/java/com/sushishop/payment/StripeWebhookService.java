package com.sushishop.payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeService stripeService;
    private final PaymentService paymentService;
    private final WebhookIdempotencyService webhookIdempotencyService;

    public void handle(String payload, String sigHeader) {
        var event = stripeService.parseCheckoutCompletedEvent(payload, sigHeader);

        if (!webhookIdempotencyService.markProcessed(event.eventId())) {
            log.info("Duplicate Stripe webhook event ignored: {}", event.eventId());
            return;
        }

        Compensation.run(
                () -> {
                    if (event.sessionId() != null) {
                        paymentService.confirmPayment(event.sessionId());
                    }
                },
                () -> webhookIdempotencyService.unmark(event.eventId())
        );
    }
}
