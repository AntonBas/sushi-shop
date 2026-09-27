package com.sushishop.security.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test_secret_key_for_testing_1234567890";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);

    @Test
    void shouldRoundTripClaimsFromGeneratedToken() {
        var token = jwtUtil.generateToken("anton@example.com", "CUSTOMER", 3);

        var payload = jwtUtil.parseToken(token);

        assertThat(payload).isNotNull();
        assertThat(payload.email()).isEqualTo("anton@example.com");
        assertThat(payload.tokenVersion()).isEqualTo(3);
        assertThat(payload.jti()).isNotBlank();
        assertThat(payload.expiration()).isInTheFuture();
    }

    @Test
    void shouldGenerateUniqueJtiPerToken() {
        var first = jwtUtil.parseToken(jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0));
        var second = jwtUtil.parseToken(jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0));

        assertThat(first.jti()).isNotEqualTo(second.jti());
    }

    @Test
    void shouldReturnNullForExpiredToken() {
        var expiredUtil = new JwtUtil(SECRET, -1_000);
        var token = expiredUtil.generateToken("anton@example.com", "CUSTOMER", 0);

        assertThat(jwtUtil.parseToken(token)).isNull();
    }

    @Test
    void shouldReturnNullForTokenSignedWithDifferentKey() {
        var otherUtil = new JwtUtil("another_secret_key_for_testing_0987654321", 60_000);
        var token = otherUtil.generateToken("anton@example.com", "CUSTOMER", 0);

        assertThat(jwtUtil.parseToken(token)).isNull();
    }

    @Test
    void shouldReturnNullForTamperedToken() {
        var token = jwtUtil.generateToken("anton@example.com", "CUSTOMER", 0);
        var parts = token.split("\\.");
        var tampered = parts[0] + "." + parts[1] + "x." + parts[2];

        assertThat(jwtUtil.parseToken(tampered)).isNull();
    }

    @Test
    void shouldReturnNullForMalformedToken() {
        assertThat(jwtUtil.parseToken("not-a-jwt")).isNull();
    }
}
