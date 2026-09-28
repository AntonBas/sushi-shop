package com.sushishop.shared.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "testcontainers"})
public class InMemoryRateLimitConfig {

    @Bean
    public ProxyManager<String> rateLimitProxyManager() {
        return new InMemoryProxyManager();
    }
}
