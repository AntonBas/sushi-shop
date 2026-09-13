package com.sushishop.shared.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LogSanitizerTest {

    @Test
    public void shouldReplaceNewlinesAndCarriageReturns() {
        assertThat(LogSanitizer.sanitize("foo\r\nFAKE LOG ENTRY: admin logged in"))
                .isEqualTo("foo__FAKE LOG ENTRY: admin logged in");
    }

    @Test
    public void shouldLeaveCleanStringUnchanged() {
        assertThat(LogSanitizer.sanitize("Weekend Sale")).isEqualTo("Weekend Sale");
    }

    @Test
    public void shouldReturnNullForNullInput() {
        assertThat(LogSanitizer.sanitize(null)).isNull();
    }
}
