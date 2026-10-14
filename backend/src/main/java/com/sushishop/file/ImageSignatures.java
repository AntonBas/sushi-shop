package com.sushishop.file;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

final class ImageSignatures {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP = "WEBP".getBytes(StandardCharsets.US_ASCII);

    private ImageSignatures() {
    }

    static boolean matches(byte[] content, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> startsWith(content, JPEG, 0);
            case "image/png" -> startsWith(content, PNG, 0);
            case "image/webp" -> startsWith(content, RIFF, 0) && startsWith(content, WEBP, 8);
            default -> false;
        };
    }

    private static boolean startsWith(byte[] content, byte[] signature, int offset) {
        return content.length >= offset + signature.length
                && Arrays.equals(content, offset, offset + signature.length, signature, 0, signature.length);
    }
}
