package com.sushishop.security.jwt;

import com.sushishop.shared.event.UserSessionsInvalidatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCacheInvalidationListener {

    private final UserCacheService userCacheService;
    private final CacheManager cacheManager;

    @EventListener
    public void onUserSessionsInvalidated(UserSessionsInvalidatedEvent event) {
        userCacheService.evictCachedUser(event.getEmail(), event.getPreviousTokenVersion());
        var usersCache = cacheManager.getCache("users");
        if (usersCache != null) {
            usersCache.evict(event.getEmail());
        }
    }
}
