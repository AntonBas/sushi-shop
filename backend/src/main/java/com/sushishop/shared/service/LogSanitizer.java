package com.sushishop.shared.service;

public final class LogSanitizer {

    private LogSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("[\r\n]", "_");
    }
}
