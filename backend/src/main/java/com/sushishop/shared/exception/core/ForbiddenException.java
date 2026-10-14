package com.sushishop.shared.exception.core;

import static org.springframework.http.HttpStatus.FORBIDDEN;

public class ForbiddenException extends SushiShopException {

    public ForbiddenException(String message, String code) {
        super(FORBIDDEN, message, code);
    }
}
