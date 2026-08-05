package com.sushishop.product;

import com.sushishop.shared.enums.Category;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"promotions", "productImages"})
    @Nonnull
    Page<Product> findAll(Specification<Product> spec, @Nonnull Pageable pageable);

    @EntityGraph(attributePaths = {"promotions", "productImages"})
    @Nonnull
    Optional<Product> findById(@Nonnull Long id);

    @EntityGraph(attributePaths = {"promotions", "productImages"})
    Optional<Product> findBySlug(String slug);

    @Query("""
                SELECT p FROM Product p
                LEFT JOIN p.reviews r
                WHERE p.available = true
                GROUP BY p
                ORDER BY AVG(r.rating) DESC, COUNT(r) DESC
            """)
    List<Product> findPopular(Pageable pageable);

    @EntityGraph(attributePaths = {"promotions", "productImages"})
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.id != :id AND p.available = true ORDER BY p.id DESC")
    List<Product> findRelated(@Param("category") Category category, @Param("id") Long id);

    @Query("SELECT r.product.id, AVG(r.rating) FROM Review r WHERE r.product.id IN :productIds GROUP BY r.product.id")
    List<Object[]> findAverageRatingsByProductIds(@Param("productIds") List<Long> productIds);
}