package com.sushishop.repository;

import com.sushishop.domain.Product;
import com.sushishop.domain.enums.Category;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecs {

    public static Specification<Product> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%");
        };
    }

    public static Specification<Product> hasCategory(Category category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) return null;
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }

    public static Specification<Product> isAvailable(Boolean available) {
        return (root, query, criteriaBuilder) -> {
            if (available == null) return null;
            return criteriaBuilder.equal(root.get("available"), available);
        };
    }
}
