package com.sushishop.exception.core;

import static org.springframework.http.HttpStatus.CONFLICT;

public class ConflictException extends SushiShopException {

    public ConflictException(String message) {
        super(CONFLICT, message);
    }
}