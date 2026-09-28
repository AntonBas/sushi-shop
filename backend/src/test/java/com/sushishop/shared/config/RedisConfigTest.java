package com.sushishop.shared.config;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;

import static org.assertj.core.api.Assertions.assertThatCode;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();
    private final Cache cache = Mockito.mock(Cache.class);
    private final RedisConnectionFailureException redisDown =
            new RedisConnectionFailureException("Redis unavailable");

    @Test
    void shouldNotPropagateOnCacheGetError() {
        assertThatCode(() -> redisConfig.errorHandler().handleCacheGetError(redisDown, cache, "key"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotPropagateOnCachePutError() {
        assertThatCode(() -> redisConfig.errorHandler().handleCachePutError(redisDown, cache, "key", "value"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotPropagateOnCacheEvictError() {
        assertThatCode(() -> redisConfig.errorHandler().handleCacheEvictError(redisDown, cache, "key"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldNotPropagateOnCacheClearError() {
        assertThatCode(() -> redisConfig.errorHandler().handleCacheClearError(redisDown, cache))
                .doesNotThrowAnyException();
    }
}
