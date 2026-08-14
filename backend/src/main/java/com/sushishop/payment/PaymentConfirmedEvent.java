package com.sushishop.payment;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentConfirmedEvent extends ApplicationEvent {
    private final Payment payment;

    public PaymentConfirmedEvent(Object source, Payment payment) {
        super(source);
        this.payment = payment;
    }
}
