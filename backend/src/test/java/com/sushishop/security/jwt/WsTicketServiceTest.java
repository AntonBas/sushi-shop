package com.sushishop.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WsTicketServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WsTicketService wsTicketService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldStoreTicketWithShortTtl() {
        var ticket = wsTicketService.issueTicket("anton@example.com", 2);

        var keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), eq("anton@example.com:2"), eq(Duration.ofSeconds(30)));
        assertThat(keyCaptor.getValue()).isEqualTo("ws:ticket:" + ticket);
    }

    @Test
    void shouldIssueUniqueTickets() {
        var first = wsTicketService.issueTicket("anton@example.com", 0);
        var second = wsTicketService.issueTicket("anton@example.com", 0);

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldConsumeTicketAtomicallyAndParsePayload() {
        when(valueOperations.getAndDelete("ws:ticket:abc")).thenReturn("anton@example.com:5");

        var payload = wsTicketService.consume("abc");

        assertThat(payload).contains(new WsTicketService.WsTicketPayload("anton@example.com", 5));
    }

    @Test
    void shouldSplitOnLastColonWhenEmailContainsColon() {
        when(valueOperations.getAndDelete("ws:ticket:abc")).thenReturn("\"a:b\"@example.com:1");

        var payload = wsTicketService.consume("abc");

        assertThat(payload).contains(new WsTicketService.WsTicketPayload("\"a:b\"@example.com", 1));
    }

    @Test
    void shouldReturnEmptyForUnknownOrAlreadyUsedTicket() {
        when(valueOperations.getAndDelete("ws:ticket:used")).thenReturn(null);

        assertThat(wsTicketService.consume("used")).isEmpty();
    }
}
