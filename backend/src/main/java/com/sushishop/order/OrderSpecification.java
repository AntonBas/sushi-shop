package com.sushishop.order;

import com.sushishop.shared.service.LikePattern;
import org.springframework.data.jpa.domain.Specification;

public class OrderSpecification {
    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Order> hasDeliveryMethod(DeliveryMethod method) {
        return (root, query, cb) -> method == null ? null : cb.equal(root.get("deliveryMethod"), method);
    }

    public static Specification<Order> hasPaymentMethod(PaymentMethod method) {
        return (root, query, cb) -> method == null ? null : cb.equal(root.get("paymentMethod"), method);
    }

    public static Specification<Order> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            return cb.or(
                    cb.like(cb.lower(root.get("customerName")), LikePattern.containsIgnoreCase(search), LikePattern.ESCAPE),
                    cb.like(root.get("phone"), LikePattern.contains(search), LikePattern.ESCAPE)
            );
        };
    }
}
