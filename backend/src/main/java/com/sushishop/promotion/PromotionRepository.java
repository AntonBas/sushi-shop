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
    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :now AND p.endDate >= :now AND p.active = true")
    List<Promotion> findActiveAt(@Param("now") LocalDateTime now);

    @Query("SELECT p.id FROM Promotion p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Long> findIdsBySearch(@Param("search") String search, @Nonnull Pageable pageable);

    @Query("SELECT p.id FROM Promotion p")
    Page<Long> findIds(@Nonnull Pageable pageable);

    @Nonnull
    @EntityGraph(attributePaths = {"products"})
    @Query("SELECT p FROM Promotion p WHERE p.id IN :ids")
    List<Promotion> findPromotionsByIds(@Param("ids") List<Long> ids);

    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"products", "products.productImages"})
    Optional<Promotion> findById(@Nonnull Long id);

    @Nonnull
    @EntityGraph(attributePaths = {"products", "products.productImages"})
    Optional<Promotion> findBySlug(String slug);

    @Nonnull
    Optional<Promotion> findByTitle(String title);
}