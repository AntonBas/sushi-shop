package com.sushishop.shared.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link RateLimitRedisConfig} bean graph itself wires up in a real Spring
 * context (not just the raw bucket4j/Lettuce calls it makes), the way it will in the
 * "local"/"docker"/"prod" profiles.
 */
@Testcontainers
class RateLimitRedisConfigTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Configuration
    static class RedisPropertiesConfig {
        @Bean
        DataRedisProperties dataRedisProperties() {
            var properties = new DataRedisProperties();
            properties.setHost(redis.getHost());
            properties.setPort(redis.getMappedPort(6379));
            return properties;
        }
    }

    @Test
    void shouldWireUpAndProduceAWorkingProxyManager() {
        new ApplicationContextRunner()
                .withUserConfiguration(RedisPropertiesConfig.class, RateLimitRedisConfig.class)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(ProxyManager.class);

                    @SuppressWarnings("unchecked")
                    var proxyManager = (ProxyManager<String>) context.getBean(ProxyManager.class);
                    var rateLimitService = new RateLimitService(proxyManager);

                    assertThat(rateLimitService.tryConsume("198.51.100.7", 1, 1, 60)).isTrue();
                    assertThat(rateLimitService.tryConsume("198.51.100.7", 1, 1, 60)).isFalse();
                });
    }
}
