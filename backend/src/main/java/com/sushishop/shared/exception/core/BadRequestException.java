package com.sushishop.shared.exception.core;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class BadRequestException extends SushiShopException {

    public BadRequestException(String message) {
        super(BAD_REQUEST, message);
    }

    public BadRequestException(String message, String code) {
        super(BAD_REQUEST, message, code);
    }
}