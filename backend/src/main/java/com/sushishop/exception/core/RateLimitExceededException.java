package com.sushishop.exception.core;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

public class RateLimitExceededException extends SushiShopException {
    public RateLimitExceededException() {
        super(TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
    }
}