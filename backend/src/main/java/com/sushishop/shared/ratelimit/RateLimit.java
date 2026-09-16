package com.sushishop.shared.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int value() default 5;

    int duration() default 60;

    /**
     * Rate limit dimensions. "ip" limits by client IP. "email" limits by the
     * email field of the request body argument (requires an argument with an
     * email() accessor). Any other value is used as a literal, global key.
     */
    String[] key() default {"ip"};
}