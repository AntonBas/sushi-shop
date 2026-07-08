package com.sushishop.repository;

import com.sushishop.domain.Token;
import com.sushishop.domain.enums.TokenType;
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
    @Query("DELETE FROM Token t WHERE t.expiryDate < :now AND t.used = false")
    int deleteAllByExpiryDateBeforeAndUsedFalse(@Param("now") LocalDateTime now);
}