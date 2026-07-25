package com.sushishop.service;

import com.sushishop.annotation.Auditable;
import com.sushishop.domain.Payment;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.domain.enums.PaymentStatus;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.repository.OrderRepository;
import com.sushishop.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Auditable(action = "CREATE", entity = "Payment")
    @Transactional
    public Payment create(Long orderId, String stripeSessionId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        var payment = Payment.builder()
                .order(order)
                .stripeSessionId(stripeSessionId)
                .amount(amount)
                .status(PaymentStatus.PENDING)
                .build();

        var saved = paymentRepository.save(payment);
        log.info("Payment created: {} for order: {}", saved.getId(), orderId);
        return saved;
    }

    @Auditable(action = "CONFIRM", entity = "Payment")
    @Transactional
    public void confirmPayment(String stripeSessionId) {
        if (stripeSessionId == null || stripeSessionId.isBlank()) {
            throw new BadRequestException("Session ID is required");
        }

        var payment = paymentRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment already confirmed");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.getOrder().setStatus(OrderStatus.CONFIRMED);
        payment.getOrder().setPaymentStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        log.info("Payment confirmed: {}", stripeSessionId);
    }
}