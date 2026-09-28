package com.sushishop.payment;

import com.stripe.Stripe;
import com.stripe.net.Webhook;
import com.sushishop.shared.exception.core.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripeServiceTest {

    private static final String WEBHOOK_SECRET = "whsec_test_secret";

    private StripeService stripeService;

    @BeforeEach
    void setUp() {
        stripeService = new StripeService();
        ReflectionTestUtils.setField(stripeService, "webhookSecret", WEBHOOK_SECRET);
    }

    @Test
    void shouldReturnSessionIdForPaidCheckoutWithMatchingApiVersion() throws Exception {
        var payload = checkoutCompletedPayload(Stripe.API_VERSION, "paid");

        var event = stripeService.parseCheckoutCompletedEvent(payload, sign(payload));

        assertThat(event.eventId()).isEqualTo("evt_1");
        assertThat(event.sessionId()).isEqualTo("cs_test_1");
    }

    @Test
    void shouldReturnSessionIdWhenEventApiVersionDiffersFromSdk() throws Exception {
        var payload = checkoutCompletedPayload("2020-08-27", "paid");

        var event = stripeService.parseCheckoutCompletedEvent(payload, sign(payload));

        assertThat(event.sessionId()).isEqualTo("cs_test_1");
    }

    @Test
    void shouldReturnNullSessionIdForUnpaidCheckout() throws Exception {
        var payload = checkoutCompletedPayload(Stripe.API_VERSION, "unpaid");

        var event = stripeService.parseCheckoutCompletedEvent(payload, sign(payload));

        assertThat(event.eventId()).isEqualTo("evt_1");
        assertThat(event.sessionId()).isNull();
    }

    @Test
    void shouldRejectInvalidSignature() {
        var payload = checkoutCompletedPayload(Stripe.API_VERSION, "paid");

        assertThatThrownBy(() -> stripeService.parseCheckoutCompletedEvent(payload, "t=1,v1=invalid"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid Stripe signature");
    }

    private static String checkoutCompletedPayload(String apiVersion, String paymentStatus) {
        return """
                {
                  "id": "evt_1",
                  "object": "event",
                  "api_version": "%s",
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_1",
                      "object": "checkout.session",
                      "payment_status": "%s"
                    }
                  }
                }
                """.formatted(apiVersion, paymentStatus);
    }

    private static String sign(String payload) throws Exception {
        long timestamp = Webhook.Util.getTimeNow();
        var signature = Webhook.Util.computeHmacSha256(WEBHOOK_SECRET, timestamp + "." + payload);
        return "t=" + timestamp + ",v1=" + signature;
    }
}
