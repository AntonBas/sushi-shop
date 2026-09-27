package com.sushishop.shared.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCancelledEvent extends ApplicationEvent {
    private final Long orderId;

    public OrderCancelledEvent(Object source, Long orderId) {
        super(source);
        this.orderId = orderId;
    }
}
