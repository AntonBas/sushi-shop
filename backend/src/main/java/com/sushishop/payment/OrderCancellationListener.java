package com.sushishop.payment;

import com.sushishop.shared.event.OrderCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancellationListener {

    private final PaymentService paymentService;
    private final StripeService stripeService;

    @TransactionalEventListener
    public void onOrderCancelled(OrderCancelledEvent event) {
        var sessionIds = paymentService.expirePendingPaymentsInNewTransaction(event.getOrderId());
        sessionIds.forEach(stripeService::expireCheckoutSession);
        if (!sessionIds.isEmpty()) {
            log.info("Expired {} pending checkout session(s) for cancelled order {}", sessionIds.size(), event.getOrderId());
        }
    }
}
