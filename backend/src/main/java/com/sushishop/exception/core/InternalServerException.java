package com.sushishop.exception.core;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class InternalServerException extends SushiShopException {

    public InternalServerException(String message) {
        super(INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(INTERNAL_SERVER_ERROR, message, cause);
    }
}