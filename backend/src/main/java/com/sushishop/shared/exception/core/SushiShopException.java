package com.sushishop.shared.exception.core;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SushiShopException extends RuntimeException {

    private final HttpStatus status;

    public SushiShopException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public SushiShopException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
