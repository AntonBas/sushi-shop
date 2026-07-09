package com.sushishop.repository;

import com.sushishop.domain.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class AuditLogSpecification {

    public static Specification<AuditLog> filter(String action, String entityName, Long entityId,
                                                 String performedBy, LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }
            if (entityName != null && !entityName.isBlank()) {
                predicates.add(cb.equal(root.get("entityName"), entityName));
            }
            if (entityId != null) {
                predicates.add(cb.equal(root.get("entityId"), entityId));
            }
            if (performedBy != null && !performedBy.isBlank()) {
                predicates.add(cb.equal(root.get("performedBy"), performedBy));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("performedAt"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("performedAt"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}