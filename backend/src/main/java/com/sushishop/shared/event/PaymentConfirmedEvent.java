package com.sushishop.shared.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentConfirmedEvent extends ApplicationEvent {
    private final Long orderId;

    public PaymentConfirmedEvent(Object source, Long orderId) {
        super(source);
        this.orderId = orderId;
    }
}
