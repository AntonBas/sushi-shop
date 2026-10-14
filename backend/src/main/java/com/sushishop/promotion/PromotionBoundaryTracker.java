package com.sushishop.promotion;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class PromotionBoundaryTracker {

    private final AtomicReference<LocalDateTime> nextBoundary = new AtomicReference<>(LocalDateTime.MIN);

    public LocalDateTime nextBoundary() {
        return nextBoundary.get();
    }

    public void update(LocalDateTime expected, LocalDateTime next) {
        nextBoundary.compareAndSet(expected, next);
    }

    public void reset() {
        nextBoundary.set(LocalDateTime.MIN);
    }
}
