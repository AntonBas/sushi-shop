package com.sushishop.payment;

import com.sushishop.shared.event.OrderCancelledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCancellationListenerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private StripeService stripeService;

    @InjectMocks
    private OrderCancellationListener listener;

    @Test
    void shouldExpirePendingPaymentsAndStripeSessionsOfCancelledOrder() {
        when(paymentService.expirePendingPaymentsInNewTransaction(7L)).thenReturn(List.of("cs_1", "cs_2"));

        listener.onOrderCancelled(new OrderCancelledEvent(this, 7L));

        verify(stripeService).expireCheckoutSession("cs_1");
        verify(stripeService).expireCheckoutSession("cs_2");
    }

    @Test
    void shouldNotCallStripeWhenNoPendingPayments() {
        when(paymentService.expirePendingPaymentsInNewTransaction(7L)).thenReturn(List.of());

        listener.onOrderCancelled(new OrderCancelledEvent(this, 7L));

        verify(stripeService, never()).expireCheckoutSession(anyString());
    }
}
