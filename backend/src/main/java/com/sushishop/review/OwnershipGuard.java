package com.sushishop.review;

import org.springframework.security.access.AccessDeniedException;

final class OwnershipGuard {

    private OwnershipGuard() {
    }

    static void requireOwner(String ownerEmail, String requesterEmail, String message) {
        if (!ownerEmail.equals(requesterEmail)) {
            throw new AccessDeniedException(message);
        }
    }
}
