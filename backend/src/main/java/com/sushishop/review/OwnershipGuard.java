package com.sushishop.review;

import org.springframework.security.access.AccessDeniedException;

final class OwnershipGuard {

    private OwnershipGuard() {
    }

    static void requireOwnerOrAdmin(String ownerEmail, String requesterEmail, boolean requesterIsAdmin, String message) {
        if (!requesterIsAdmin) {
            requireOwner(ownerEmail, requesterEmail, message);
        }
    }

    static void requireOwner(String ownerEmail, String requesterEmail, String message) {
        if (!ownerEmail.equals(requesterEmail)) {
            throw new AccessDeniedException(message);
        }
    }
}
