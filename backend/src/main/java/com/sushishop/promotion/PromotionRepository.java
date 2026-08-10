package com.sushishop.promotion;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Nonnull
    @EntityGraph(attributePaths = {"products", "products.productImages"})
    List<Promotion> findByStartDateBeforeAndEndDateAfter(LocalDateTime start, LocalDateTime end);

    @Nonnull
    @EntityGraph(attributePaths = {"products"})
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Promotion> findAllBySearch(@Param("search") String search, @Nonnull Pageable pageable);

    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"products"})
    Page<Promotion> findAll(@Nonnull Pageable pageable);

    @Nonnull
    @EntityGraph(attributePaths = {"products", "products.productImages"})
    Optional<Promotion> findBySlug(String slug);

    Optional<Promotion> findByTitle(String title);
}