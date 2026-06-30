package com.sushishop.repository;

import com.sushishop.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByActiveTrueAndEndDateAfter(LocalDateTime now);
}
