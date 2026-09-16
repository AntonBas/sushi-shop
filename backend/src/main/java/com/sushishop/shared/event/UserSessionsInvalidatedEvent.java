package com.sushishop.shared.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserSessionsInvalidatedEvent extends ApplicationEvent {
    private final String email;
    private final Integer previousTokenVersion;

    public UserSessionsInvalidatedEvent(Object source, String email, Integer previousTokenVersion) {
        super(source);
        this.email = email;
        this.previousTokenVersion = previousTokenVersion;
    }
}
