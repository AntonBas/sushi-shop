package com.sushishop.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT r.id FROM Review r WHERE r.product.id = :productId")
    Page<Long> findReviewIdsByProductId(@Param("productId") Long productId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "replies", "replies.user"})
    @Query("SELECT r FROM Review r WHERE r.id IN :ids ORDER BY r.createdAt DESC")
    List<Review> findReviewsByIds(@Param("ids") List<Long> ids);
}