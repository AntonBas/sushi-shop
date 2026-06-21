package com.sushishop.exception.core;

import static org.springframework.http.HttpStatus.NOT_FOUND;

public class NotFoundException extends SushiShopException {

    public NotFoundException(String resource, Long id) {
        super(NOT_FOUND, resource + " not found with id: " + id);
    }

    public NotFoundException(String message) {
        super(NOT_FOUND, message);
    }
}