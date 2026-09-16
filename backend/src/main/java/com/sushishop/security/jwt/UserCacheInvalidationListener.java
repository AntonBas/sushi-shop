package com.sushishop.security.jwt;

import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCacheInvalidationListener {

    private final UserCacheService userCacheService;

    @EventListener
    public void onUserSessionsInvalidated(UserSessionsInvalidatedEvent event) {
        userCacheService.evictCachedUser(event.getEmail(), event.getPreviousTokenVersion());
    }
}
