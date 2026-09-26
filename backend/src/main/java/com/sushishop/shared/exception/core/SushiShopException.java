package com.sushishop.shared.exception.core;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SushiShopException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public SushiShopException(HttpStatus status, String message) {
        this(status, message, (String) null);
    }

    public SushiShopException(HttpStatus status, String message, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public SushiShopException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = null;
    }
}
