package com.sushishop.file;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ImageSignaturesTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0};
    private static final byte[] WEBP = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.US_ASCII);

    @Test
    void shouldMatchSupportedImageSignatures() {
        assertThat(ImageSignatures.matches(JPEG, "image/jpeg")).isTrue();
        assertThat(ImageSignatures.matches(PNG, "image/png")).isTrue();
        assertThat(ImageSignatures.matches(WEBP, "image/webp")).isTrue();
    }

    @Test
    void shouldRejectContentOfAnotherImageType() {
        assertThat(ImageSignatures.matches(PNG, "image/jpeg")).isFalse();
        assertThat(ImageSignatures.matches(JPEG, "image/webp")).isFalse();
    }

    @Test
    void shouldRejectTruncatedContent() {
        assertThat(ImageSignatures.matches(new byte[]{(byte) 0x89, 'P'}, "image/png")).isFalse();
        assertThat(ImageSignatures.matches("RIFF".getBytes(StandardCharsets.US_ASCII), "image/webp")).isFalse();
    }

    @Test
    void shouldRejectUnsupportedType() {
        assertThat(ImageSignatures.matches(JPEG, "image/gif")).isFalse();
    }
}
