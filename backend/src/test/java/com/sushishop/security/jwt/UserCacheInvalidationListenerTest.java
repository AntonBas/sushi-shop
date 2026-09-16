package com.sushishop.security.jwt;

import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserCacheInvalidationListenerTest {

    @Mock
    private UserCacheService userCacheService;

    @InjectMocks
    private UserCacheInvalidationListener listener;

    @Test
    void shouldEvictCachedUserOnEvent() {
        var event = new UserSessionsInvalidatedEvent(this, "anton@example.com", 3);

        listener.onUserSessionsInvalidated(event);

        verify(userCacheService).evictCachedUser("anton@example.com", 3);
    }
}
