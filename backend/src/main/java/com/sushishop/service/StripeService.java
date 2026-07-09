package com.sushishop.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.sushishop.exception.core.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripeService {

    @Value("${app.stripe.secret-key}")
    private String secretKey;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public String createCheckoutSession(Long orderId, Long amountInCents, String email) {
        var params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/orders/" + orderId + "?success=true")
                .setCancelUrl(baseUrl + "/orders/" + orderId + "?canceled=true")
                .setCustomerEmail(email)
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("uah")
                                .setUnitAmount(amountInCents)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Order #" + orderId)
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            var session = Session.create(params);
            log.info("Stripe session created: {} for order: {}", session.getId(), orderId);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Failed to create Stripe session for order: {}", orderId, e);
            throw new RuntimeException("Payment session creation failed", e);
        }
    }

    public String getSessionIdFromWebhook(String payload, String sigHeader) {
        try {
            var event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            if ("checkout.session.completed".equals(event.getType())) {
                var session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
                return session.getId();
            }
            return null;
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid Stripe signature");
        }
    }
}