package com.sushishop.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CsrfHeaderFilterTest {

    private final CsrfHeaderFilter filter = new CsrfHeaderFilter();

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void shouldRejectStateChangingRequestWithoutHeader(String method) throws Exception {
        var request = new MockHttpServletRequest(method, "/api/orders");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getErrorMessage()).isEqualTo("Missing required header: X-Requested-With");
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void shouldAllowStateChangingRequestWithHeader() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/orders");
        request.addHeader("X-Requested-With", "XMLHttpRequest");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isSameAs(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "HEAD", "OPTIONS", "TRACE"})
    void shouldAllowSafeMethodsWithoutHeader(String method) throws Exception {
        var request = new MockHttpServletRequest(method, "/api/products");
        var chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void shouldExemptStripeWebhookFromHeaderCheck() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/payments/webhook");
        var chain = new MockFilterChain();

        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isSameAs(request);
    }

    @Test
    void shouldNotExemptOtherPaymentEndpoints() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/payments/order/1");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(chain.getRequest()).isNull();
    }
}
