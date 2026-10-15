package com.sushishop.payment;

import com.sushishop.order.Order;
import com.sushishop.order.OrderService;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    private static final String EMAIL = "test@example.com";

    @Mock
    private StripeService stripeService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private OrderService orderService;

    @InjectMocks
    private CheckoutService checkoutService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder().id(1L).totalAmount(new BigDecimal("500.00")).build();
        when(orderService.getOwnedOrder(1L, EMAIL, false)).thenReturn(order);
    }

    @Test
    void shouldCreateCheckoutSessionAndPaymentInCents() {
        when(stripeService.createCheckoutSession(1L, 50000L, EMAIL))
                .thenReturn(new StripeService.CheckoutSessionInfo("sess_123", "https://checkout.stripe.com/session_123"));

        var url = checkoutService.createCheckout(1L, EMAIL);

        assertThat(url).isEqualTo("https://checkout.stripe.com/session_123");
        verify(paymentService).create(1L, "sess_123", new BigDecimal("500.00"));
    }

    @Test
    void shouldExpirePreviousPendingSessionsBeforeCreatingNewOne() {
        when(paymentService.expirePendingPayments(1L)).thenReturn(List.of("sess_old"));
        when(stripeService.createCheckoutSession(1L, 50000L, EMAIL))
                .thenReturn(new StripeService.CheckoutSessionInfo("sess_new", "https://checkout.stripe.com/new"));

        checkoutService.createCheckout(1L, EMAIL);

        verify(stripeService).expireCheckoutSession("sess_old");
    }

    @Test
    void shouldNotCreateStripeSessionForNonPayableOrder() {
        doThrow(new ConflictException("Order is already paid")).when(paymentService).ensurePayable(order);

        assertThatThrownBy(() -> checkoutService.createCheckout(1L, EMAIL)).isInstanceOf(ConflictException.class);

        verify(stripeService, never()).createCheckoutSession(any(), any(), any());
    }

    @Test
    void shouldExpireStripeSessionWhenPaymentCreationFails() {
        when(stripeService.createCheckoutSession(1L, 50000L, EMAIL))
                .thenReturn(new StripeService.CheckoutSessionInfo("sess_123", "https://checkout.stripe.com/session_123"));
        doThrow(new BadRequestException("Order not found")).when(paymentService).create(any(), any(), any());

        assertThatThrownBy(() -> checkoutService.createCheckout(1L, EMAIL)).isInstanceOf(BadRequestException.class);

        verify(stripeService).expireCheckoutSession("sess_123");
    }
}
