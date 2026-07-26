package com.sushishop.shared.exception.core;

import static org.springframework.http.HttpStatus.FORBIDDEN;

public class ForbiddenException extends SushiShopException {

    public ForbiddenException(String message) {
        super(FORBIDDEN, message);
    }
}