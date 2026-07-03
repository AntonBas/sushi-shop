package com.sushishop.service;

import com.sushishop.domain.Payment;
import com.sushishop.domain.enums.PaymentStatus;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.repository.OrderRepository;
import com.sushishop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment create(Long orderId, String stripeSessionId, BigDecimal amount) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        var payment = Payment.builder()
                .order(order)
                .stripeSessionId(stripeSessionId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();
        return paymentRepository.save(payment);
    }

    public void confirmPayment(String stripeSessionId) {
        var payment = paymentRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        log.info("Payment confirmed: {}", stripeSessionId);
    }
}