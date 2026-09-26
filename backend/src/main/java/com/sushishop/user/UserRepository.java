package com.sushishop.user;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailForUpdate(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoff"
            + " AND NOT EXISTS (SELECT 1 FROM Order o WHERE o.user = u)"
            + " AND NOT EXISTS (SELECT 1 FROM Review r WHERE r.user = u)"
            + " AND NOT EXISTS (SELECT 1 FROM ReviewReply rr WHERE rr.user = u)")
    int deleteUnverifiedWithoutActivityCreatedBefore(@Param("cutoff") LocalDateTime cutoff);
}
