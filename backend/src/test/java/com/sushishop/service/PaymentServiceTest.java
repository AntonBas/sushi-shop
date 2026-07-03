package com.sushishop.service;

import com.sushishop.domain.Order;
import com.sushishop.domain.Payment;
import com.sushishop.domain.enums.PaymentStatus;
import com.sushishop.repository.OrderRepository;
import com.sushishop.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    void shouldConfirmPayment() {
        var payment = Payment.builder().stripeSessionId("sess_123").status(PaymentStatus.PENDING).build();

        when(paymentRepository.findByStripeSessionId("sess_123")).thenReturn(Optional.of(payment));

        paymentService.confirmPayment("sess_123");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(paymentRepository).save(payment);
    }
}