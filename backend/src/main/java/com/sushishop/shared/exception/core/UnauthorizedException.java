package com.sushishop.shared.exception.core;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class UnauthorizedException extends SushiShopException {

    public UnauthorizedException(String message) {
        super(UNAUTHORIZED, message);
    }
}