package com.sushishop.payment;

import com.sushishop.audit.Auditable;
import com.sushishop.order.Order;
import com.sushishop.order.OrderService;
import com.sushishop.order.OrderStatus;
import com.sushishop.order.PaymentMethod;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.event.PaymentConfirmedEvent;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.ConflictException;
import com.sushishop.shared.exception.core.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    public void ensurePayable(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new ConflictException("Order is paid on delivery");
        }
        if (order.isPaid()) {
            throw new ConflictException("Order is already paid");
        }
        if (order.getStatus() != OrderStatus.NEW) {
            throw new ConflictException("Order cannot be paid in status " + order.getStatus());
        }
    }

    @Transactional
    public List<String> expirePendingPayments(Long orderId) {
        var pending = paymentRepository.findByOrderIdAndStatus(orderId, PaymentStatus.PENDING);
        pending.forEach(payment -> payment.setStatus(PaymentStatus.EXPIRED));
        paymentRepository.saveAll(pending);
        return pending.stream().map(Payment::getStripeSessionId).toList();
    }

    @Auditable(action = AuditAction.CREATE, entity = "Payment")
    @Transactional
    public Payment create(Long orderId, String stripeSessionId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero");
        }

        var order = orderService.getOrderByIdInternal(orderId);

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
            log.info("Payment already confirmed, skipping: {}", stripeSessionId);
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);
        eventPublisher.publishEvent(new PaymentConfirmedEvent(this, payment.getOrder().getId()));
        log.info("Payment confirmed: {}", stripeSessionId);
    }
}