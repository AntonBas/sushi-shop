package com.sushishop.payment;

import com.stripe.Stripe;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.InternalServerException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StripeService {

    @Value("${app.stripe.secret-key}")
    private String secretKey;

    @Value("${app.frontend-url}")
    private String baseUrl;

    @Value("${app.stripe.webhook-secret}")
    private String webhookSecret;

    public record CheckoutSessionInfo(String id, String url) {
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public CheckoutSessionInfo createCheckoutSession(Long orderId, Long amountInCents, String email) {
        var params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(baseUrl + "/order-success?orderId=" + orderId)
                .setCancelUrl(baseUrl + "/order-cancel?orderId=" + orderId)
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
            return new CheckoutSessionInfo(session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe session for order: {}", orderId, e);
            throw new InternalServerException("Payment session creation failed", e);
        }
    }

    public void expireCheckoutSession(String sessionId) {
        try {
            Session.retrieve(sessionId).expire();
            log.info("Expired orphaned Stripe session: {}", sessionId);
        } catch (StripeException e) {
            log.error("Failed to expire orphaned Stripe session: {}", sessionId, e);
        }
    }

    public record WebhookEvent(String eventId, String sessionId) {
    }

    public WebhookEvent parseCheckoutCompletedEvent(String payload, String sigHeader) {
        try {
            var event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String sessionId = null;
            if ("checkout.session.completed".equals(event.getType())) {
                var session = deserializeSession(event);
                if ("paid".equals(session.getPaymentStatus())) {
                    sessionId = session.getId();
                } else {
                    log.warn("Checkout session {} completed with payment status {}", session.getId(), session.getPaymentStatus());
                }
            }
            return new WebhookEvent(event.getId(), sessionId);
        } catch (SignatureVerificationException e) {
            throw new BadRequestException("Invalid Stripe signature");
        }
    }

    private Session deserializeSession(Event event) {
        var deserializer = event.getDataObjectDeserializer();
        var object = deserializer.getObject();
        if (object.isPresent()) {
            return (Session) object.get();
        }
        log.error("Stripe event {} has API version {}, SDK expects {}; falling back to unsafe deserialization",
                event.getId(), event.getApiVersion(), Stripe.API_VERSION);
        try {
            return (Session) deserializer.deserializeUnsafe();
        } catch (EventDataObjectDeserializationException e) {
            throw new InternalServerException("Failed to deserialize Stripe event " + event.getId(), e);
        }
    }
}
