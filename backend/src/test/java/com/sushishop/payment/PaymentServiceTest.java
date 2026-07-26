package com.sushishop.payment;

import com.sushishop.order.Order;
import com.sushishop.shared.enums.OrderStatus;
import com.sushishop.shared.enums.PaymentStatus;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.order.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void shouldCreatePayment() {
        var order = new Order();
        order.setId(1L);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
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
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.create(99L, "sess_123", new BigDecimal("500.00")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldConfirmPayment() {
        var order = new Order();
        var payment = Payment.builder()
                .stripeSessionId("sess_123")
                .status(PaymentStatus.PENDING)
                .order(order)
                .build();

        when(paymentRepository.findByStripeSessionId("sess_123")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment("sess_123");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(paymentRepository).save(payment);
    }

    @Test
    void shouldThrowWhenConfirmAlreadyPaid() {
        var payment = Payment.builder()
                .stripeSessionId("sess_123")
                .status(PaymentStatus.PAID)
                .build();

        when(paymentRepository.findByStripeSessionId("sess_123")).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.confirmPayment("sess_123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Payment already confirmed");
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
}