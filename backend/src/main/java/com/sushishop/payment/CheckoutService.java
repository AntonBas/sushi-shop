package com.sushishop.payment;

import com.sushishop.order.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final StripeService stripeService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    public String createCheckout(Long orderId, String email) {
        var order = orderService.getOwnedOrder(orderId, email, false);
        paymentService.ensurePayable(order);
        paymentService.expirePendingPayments(orderId).forEach(stripeService::expireCheckoutSession);

        var amountInCents = order.getTotalAmount().movePointRight(2).longValueExact();
        var info = stripeService.createCheckoutSession(orderId, amountInCents, email);
        Compensation.run(
                () -> paymentService.create(orderId, info.id(), order.getTotalAmount()),
                () -> stripeService.expireCheckoutSession(info.id())
        );
        log.info("Checkout session created for order: {}", orderId);
        return info.url();
    }
}
