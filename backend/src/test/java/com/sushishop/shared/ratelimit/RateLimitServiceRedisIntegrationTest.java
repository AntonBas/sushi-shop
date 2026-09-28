package com.sushishop.shared.ratelimit;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RateLimitServiceRedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static RedisClient clientA;
    static RedisClient clientB;
    static StatefulRedisConnection<String, byte[]> connectionA;
    static StatefulRedisConnection<String, byte[]> connectionB;
    static RateLimitService instanceA;
    static RateLimitService instanceB;

    @BeforeAll
    static void setUp() {
        clientA = RedisClient.create("redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
        clientB = RedisClient.create("redis://" + redis.getHost() + ":" + redis.getMappedPort(6379));
        var codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        connectionA = clientA.connect(codec);
        connectionB = clientB.connect(codec);
        instanceA = new RateLimitService(buildProxyManager(connectionA));
        instanceB = new RateLimitService(buildProxyManager(connectionB));
    }

    @AfterAll
    static void tearDown() {
        connectionA.close();
        connectionB.close();
        clientA.shutdown();
        clientB.shutdown();
    }

    @SuppressWarnings("deprecation")
    private static ProxyManager<String> buildProxyManager(StatefulRedisConnection<String, byte[]> connection) {
        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofHours(1)))
                .build();
    }

    @Test
    void shouldShareRateLimitStateAcrossInstancesViaRedis() {
        String key = "203.0.113.5";

        assertThat(instanceA.tryConsume(key, 1, 2, 60)).isTrue();
        assertThat(instanceB.tryConsume(key, 1, 2, 60)).isTrue();

        assertThat(instanceA.tryConsume(key, 1, 2, 60)).isFalse();
        assertThat(instanceB.tryConsume(key, 1, 2, 60)).isFalse();
    }
}
