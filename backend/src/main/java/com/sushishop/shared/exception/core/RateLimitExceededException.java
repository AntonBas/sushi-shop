package com.sushishop.shared.exception.core;

import lombok.Getter;

import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;

@Getter
public class RateLimitExceededException extends SushiShopException {

    private final Long retryAfterSeconds;

    public RateLimitExceededException() {
        super(TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
        this.retryAfterSeconds = null;
    }

    public RateLimitExceededException(long retryAfterSeconds) {
        super(TOO_MANY_REQUESTS, "Too many requests. Please try again later.");
        this.retryAfterSeconds = retryAfterSeconds;
    }
}