package com.sushishop.repository;

import com.sushishop.domain.Product;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"promotions", "productImages"})
    @Nonnull
    Page<Product> findAll(Specification<Product> spec, @Nonnull Pageable pageable);

    @EntityGraph(attributePaths = {"promotions", "productImages", "reviews"})
    @Nonnull
    Optional<Product> findById(@Nonnull Long id);

    @Query("""
                SELECT p FROM Product p
                LEFT JOIN p.reviews r
                WHERE p.available = true
                GROUP BY p
                ORDER BY AVG(r.rating) DESC, COUNT(r) DESC
            """)
    List<Product> findPopular(Pageable pageable);
}