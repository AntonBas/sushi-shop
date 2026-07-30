package com.sushishop.payment;

import com.sushishop.annotation.Auditable;
import com.sushishop.order.OrderService;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.enums.PaymentStatus;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
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
    private final OrderService orderService;

    @Auditable(action = AuditAction.CREATE, entity = "Payment")
    @Transactional
    public Payment create(Long orderId, String stripeSessionId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        var order = orderService.getOrderById(orderId);

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

    @Auditable(action = AuditAction.UPDATE, entity = "Payment")
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
        paymentRepository.save(payment);
        orderService.confirmOrder(payment.getOrder().getId());
        log.info("Payment confirmed: {}", stripeSessionId);
    }
}