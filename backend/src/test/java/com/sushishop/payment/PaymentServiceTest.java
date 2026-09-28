package com.sushishop.payment;

import com.sushishop.order.Order;
import com.sushishop.order.OrderService;
import com.sushishop.order.OrderStatus;
import com.sushishop.order.PaymentMethod;
import com.sushishop.shared.event.PaymentConfirmedEvent;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() {
        var order = new Order();
        order.setId(1L);

        when(orderService.getOrderByIdInternal(1L)).thenReturn(order);
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = paymentService.create(1L, "sess_123", new BigDecimal("500.00"));

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(result.getStripeSessionId()).isEqualTo("sess_123");
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> paymentService.create(1L, "sess_123", BigDecimal.ZERO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldThrowWhenAmountIsNull() {
        assertThatThrownBy(() -> paymentService.create(1L, "sess_123", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Amount must be greater than zero");
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderService.getOrderByIdInternal(99L)).thenThrow(new NotFoundException("Order not found: 99"));

        assertThatThrownBy(() -> paymentService.create(99L, "sess_123", new BigDecimal("500.00")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldConfirmPayment() {
        var order = new Order();
        order.setId(1L);
        var payment = Payment.builder()
                .stripeSessionId("sess_123")
                .status(PaymentStatus.PENDING)
                .order(order)
                .build();

        when(paymentRepository.findByStripeSessionId("sess_123")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment("sess_123");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentRepository).save(payment);
        verify(eventPublisher).publishEvent(any(PaymentConfirmedEvent.class));
    }

    @Test
    void shouldSkipWhenConfirmAlreadyPaid() {
        var payment = Payment.builder()
                .stripeSessionId("sess_123")
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.findByStripeSessionId("sess_123")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment("sess_123");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldThrowWhenSessionIdIsNull() {
        assertThatThrownBy(() -> paymentService.confirmPayment(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Session ID is required");
    }

    @Test
    void shouldThrowWhenSessionIdIsBlank() {
        assertThatThrownBy(() -> paymentService.confirmPayment("   "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Session ID is required");
    }

    @Test
    void shouldAllowPayingNewUnpaidOnlineOrder() {
        var order = Order.builder().paymentMethod(PaymentMethod.ONLINE).status(OrderStatus.NEW).build();

        paymentService.ensurePayable(order);
    }

    @Test
    void shouldRejectPayingOnDeliveryOrder() {
        var order = Order.builder().paymentMethod(PaymentMethod.ON_DELIVERY).status(OrderStatus.NEW).build();

        assertThatThrownBy(() -> paymentService.ensurePayable(order)).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldRejectPayingAlreadyPaidOrder() {
        var order = Order.builder().paymentMethod(PaymentMethod.ONLINE).status(OrderStatus.NEW).paid(true).build();

        assertThatThrownBy(() -> paymentService.ensurePayable(order)).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldRejectPayingCancelledOrder() {
        var order = Order.builder().paymentMethod(PaymentMethod.ONLINE).status(OrderStatus.CANCELLED).build();

        assertThatThrownBy(() -> paymentService.ensurePayable(order)).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldMarkPendingPaymentsExpiredAndReturnTheirSessions() {
        var first = Payment.builder().stripeSessionId("sess_1").status(PaymentStatus.PENDING).build();
        var second = Payment.builder().stripeSessionId("sess_2").status(PaymentStatus.PENDING).build();
        when(paymentRepository.findByOrderIdAndStatus(1L, PaymentStatus.PENDING)).thenReturn(List.of(first, second));

        var sessions = paymentService.expirePendingPayments(1L);

        assertThat(sessions).containsExactly("sess_1", "sess_2");
        assertThat(first.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(second.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
    }
}
