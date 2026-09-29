package com.sushishop.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                          ObjectProvider<RedisCacheManagerBuilderCustomizer> customizers) {
        RedisCacheConfiguration defaultConfig = defaultCacheConfig();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("products", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigurations.put("promotions", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("users", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("userCache", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        var builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations);
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    public static RedisCacheConfiguration listCacheConfig(Class<?> elementType, Duration ttl) {
        var mapper = JsonMapper.builder().build();
        var listType = mapper.getTypeFactory().constructCollectionType(List.class, elementType);
        var serializer = new JacksonJsonRedisSerializer<List<?>>(mapper, listType);
        return defaultCacheConfig()
                .entryTtl(ttl)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }

    private static RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(RedisSerializer.json())
                );
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis GET failed for cache '{}', falling back to source", cache.getName(), exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis PUT failed for cache '{}', value will not be cached", cache.getName(), exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis EVICT failed for cache '{}', entry may be stale until TTL expiry", cache.getName(), exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis CLEAR failed for cache '{}'", cache.getName(), exception);
            }
        };
    }
}
