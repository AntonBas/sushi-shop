package com.sushishop.promotion;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    List<Promotion> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);

    @Query("SELECT p FROM Promotion p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Promotion> findAllBySearch(@Param("search") String search, Pageable pageable);
}