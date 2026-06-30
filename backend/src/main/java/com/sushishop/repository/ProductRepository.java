package com.sushishop.repository;

import com.sushishop.domain.Product;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"promotions"})
    @Nonnull
    Page<Product> findAll(@Nonnull Pageable pageable);

    @EntityGraph(attributePaths = {"promotions"})
    @Nonnull
    Optional<Product> findById(@Nonnull Long id);
}