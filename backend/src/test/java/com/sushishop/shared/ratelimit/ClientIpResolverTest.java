package com.sushishop.shared.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    private static final String SECRET = "proxy-secret";

    private final ClientIpResolver resolver = new ClientIpResolver("CF-Connecting-IP", SECRET);

    @Test
    void shouldUseFirstForwardedForAddressWhenRequestComesFromTrustedProxy() {
        var request = proxiedRequest(SECRET);

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.7");
    }

    @Test
    void shouldIgnoreForwardedForWhenProxySecretIsWrong() {
        var request = proxiedRequest("guess");

        assertThat(resolver.resolve(request)).isEqualTo("76.76.21.21");
    }

    @Test
    void shouldIgnoreForwardedForWhenProxySecretIsMissing() {
        var request = proxiedRequest(null);

        assertThat(resolver.resolve(request)).isEqualTo("76.76.21.21");
    }

    @Test
    void shouldNotTrustAnyProxyWhenSecretIsNotConfigured() {
        var request = proxiedRequest("");

        assertThat(new ClientIpResolver("CF-Connecting-IP", "").resolve(request)).isEqualTo("76.76.21.21");
    }

    @Test
    void shouldFallBackToRemoteAddrWhenTrustedProxyOmitsForwardedFor() {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.7");
        request.addHeader(ClientIpResolver.PROXY_SECRET_HEADER, SECRET);

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.7");
    }

    private MockHttpServletRequest proxiedRequest(String secret) {
        var request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        request.addHeader("X-Forwarded-For", "198.51.100.7, 76.76.21.21");
        request.addHeader("CF-Connecting-IP", "76.76.21.21");
        if (secret != null) {
            request.addHeader(ClientIpResolver.PROXY_SECRET_HEADER, secret);
        }
        return request;
    }
}
