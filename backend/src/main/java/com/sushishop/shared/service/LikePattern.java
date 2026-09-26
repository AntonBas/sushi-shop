package com.sushishop.shared.service;

import java.util.Locale;

public final class LikePattern {

    public static final char ESCAPE = '\\';

    private LikePattern() {
    }

    public static String contains(String value) {
        String escaped = value
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    public static String containsIgnoreCase(String value) {
        return contains(value.toLowerCase(Locale.ROOT));
    }
}
