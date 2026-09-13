package com.sushishop.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

public class CorsConfigTest {

    private CorsConfiguration configFor(String allowedOrigins) {
        CorsConfig corsConfig = new CorsConfig();
        ReflectionTestUtils.setField(corsConfig, "allowedOrigins", allowedOrigins);
        UrlBasedCorsConfigurationSource source =
                (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
        return source.getCorsConfigurations().get("/**");
    }

    @Test
    public void shouldMatchVercelPreviewDeploymentsViaWildcardPattern() {
        CorsConfiguration config = configFor("https://sushi-shop*.vercel.app");

        assertThat(config.checkOrigin("https://sushi-shop-five.vercel.app")).isNotNull();
        assertThat(config.checkOrigin("https://sushi-shop-457dbr5uq-sushi-shop1.vercel.app")).isNotNull();
    }

    @Test
    public void shouldRejectOriginNotMatchingAnyPattern() {
        CorsConfiguration config = configFor("https://sushi-shop*.vercel.app");

        assertThat(config.checkOrigin("https://evil.com")).isNull();
    }

    @Test
    public void shouldTrimWhitespaceBetweenCommaSeparatedOrigins() {
        CorsConfiguration config = configFor("https://a.com, https://b.com");

        assertThat(config.checkOrigin("https://b.com")).isNotNull();
    }
}
