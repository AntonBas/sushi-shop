package com.sushishop.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    @Modifying
    @Query("UPDATE Token t SET t.used = true WHERE t.user.id = :userId AND t.tokenType = :type AND t.used = false")
    void invalidateAllByUserAndType(@Param("userId") Long userId, @Param("type") TokenType type);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.expiryDate < :now")
    int deleteAllByExpiryDateBefore(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Token t WHERE t.user.id IN (SELECT u.id FROM User u"
            + " WHERE u.emailVerified = false AND u.createdAt < :cutoff"
            + " AND NOT EXISTS (SELECT 1 FROM Order o WHERE o.user = u)"
            + " AND NOT EXISTS (SELECT 1 FROM Review r WHERE r.user = u)"
            + " AND NOT EXISTS (SELECT 1 FROM ReviewReply rr WHERE rr.user = u))")
    void deleteAllOfUnverifiedUsersWithoutActivityCreatedBefore(@Param("cutoff") LocalDateTime cutoff);
}