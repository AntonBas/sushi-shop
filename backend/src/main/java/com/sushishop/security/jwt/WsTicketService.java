package com.sushishop.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WsTicketService {

    private static final String KEY_PREFIX = "ws:ticket:";
    private static final Duration TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;

    public record WsTicketPayload(String email, Integer tokenVersion) {
    }

    public String issueTicket(String email, Integer tokenVersion) {
        String ticket = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, email + ":" + tokenVersion, TTL);
        return ticket;
    }

    public Optional<WsTicketPayload> consume(String ticket) {
        String value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + ticket);
        if (value == null) {
            return Optional.empty();
        }
        int separatorIndex = value.lastIndexOf(':');
        String email = value.substring(0, separatorIndex);
        Integer tokenVersion = Integer.valueOf(value.substring(separatorIndex + 1));
        return Optional.of(new WsTicketPayload(email, tokenVersion));
    }
}
