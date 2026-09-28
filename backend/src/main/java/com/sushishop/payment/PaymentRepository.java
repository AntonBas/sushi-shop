package com.sushishop.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripeSessionId(String sessionId);

    List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);
}
