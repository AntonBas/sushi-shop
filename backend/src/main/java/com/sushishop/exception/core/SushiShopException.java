package com.sushishop.exception.core;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SushiShopException extends RuntimeException {

    private final HttpStatus status;
    private final String debugMessage;

    public SushiShopException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.debugMessage = null;
    }

    public SushiShopException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.debugMessage = cause.getLocalizedMessage();
    }
}