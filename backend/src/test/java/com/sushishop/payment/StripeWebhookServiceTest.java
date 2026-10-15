package com.sushishop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

    @Mock
    private StripeService stripeService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private WebhookIdempotencyService webhookIdempotencyService;

    @InjectMocks
    private StripeWebhookService stripeWebhookService;

    @BeforeEach
    void setUp() {
        when(stripeService.parseCheckoutCompletedEvent("{}", "sig_123"))
                .thenReturn(new StripeService.WebhookEvent("evt_123", "sess_123"));
    }

    @Test
    void shouldConfirmPaymentForNewEvent() {
        when(webhookIdempotencyService.markProcessed("evt_123")).thenReturn(true);

        stripeWebhookService.handle("{}", "sig_123");

        verify(paymentService).confirmPayment("sess_123");
        verify(webhookIdempotencyService, never()).unmark(anyString());
    }

    @Test
    void shouldSkipDuplicateEvent() {
        when(webhookIdempotencyService.markProcessed("evt_123")).thenReturn(false);

        stripeWebhookService.handle("{}", "sig_123");

        verify(paymentService, never()).confirmPayment(anyString());
    }

    @Test
    void shouldUnmarkEventAndRethrowWhenConfirmPaymentFails() {
        when(webhookIdempotencyService.markProcessed("evt_123")).thenReturn(true);
        doThrow(new RuntimeException("transient DB error")).when(paymentService).confirmPayment("sess_123");

        assertThatThrownBy(() -> stripeWebhookService.handle("{}", "sig_123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("transient DB error");

        verify(webhookIdempotencyService).unmark("evt_123");
    }
}
