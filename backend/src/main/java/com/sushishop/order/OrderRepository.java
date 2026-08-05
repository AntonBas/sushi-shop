package com.sushishop.order;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Override
    @EntityGraph(attributePaths = {"items", "items.product", "user"})
    @Nonnull
    Page<Order> findAll(@Nonnull Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE o.user.email = :email ORDER BY o.createdAt DESC")
    Page<Order> findByUserEmail(@Param("email") String email, Pageable pageable);
}