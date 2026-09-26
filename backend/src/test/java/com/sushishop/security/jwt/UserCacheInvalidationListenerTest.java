package com.sushishop.security.jwt;

import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCacheInvalidationListenerTest {

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache usersCache;

    @InjectMocks
    private UserCacheInvalidationListener listener;

    @Test
    void shouldEvictCachedUserOnEvent() {
        var event = new UserSessionsInvalidatedEvent(this, "anton@example.com", 3);
        when(cacheManager.getCache("users")).thenReturn(usersCache);

        listener.onUserSessionsInvalidated(event);

        verify(userCacheService).evictCachedUser("anton@example.com", 3);
        verify(usersCache).evict("anton@example.com");
    }
}
