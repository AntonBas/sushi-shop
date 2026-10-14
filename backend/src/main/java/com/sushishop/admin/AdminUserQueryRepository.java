package com.sushishop.admin;

import com.sushishop.admin.dto.response.AdminUserResponse;
import com.sushishop.order.OrderStatus;
import com.sushishop.shared.service.LikePattern;
import com.sushishop.user.UserRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AdminUserQueryRepository {

    private static final String TOTAL_SPENT = "COALESCE(SUM(CASE WHEN o.status = :delivered THEN o.totalAmount END), 0)";

    private static final Map<String, String> SORT_EXPRESSIONS = Map.of(
            "createdAt", "u.createdAt",
            "name", "LOWER(u.name)",
            "email", "u.email",
            "ordersCount", "COUNT(o.id)",
            "totalSpent", TOTAL_SPENT
    );

    private final EntityManager entityManager;

    public Page<AdminUserResponse> search(AdminUserFilter filter, Pageable pageable) {
        var parameters = new HashMap<String, Object>();
        String where = buildWhere(filter, parameters);

        TypedQuery<Tuple> query = entityManager.createQuery(
                "SELECT u.id AS id, u.name AS name, u.email AS email, u.phone AS phone, u.userRole AS userRole,"
                        + " u.emailVerified AS emailVerified,"
                        + " CASE WHEN u.password IS NULL THEN false ELSE true END AS hasPassword,"
                        + " u.createdAt AS createdAt, COUNT(o.id) AS ordersCount, " + TOTAL_SPENT + " AS totalSpent"
                        + " FROM User u LEFT JOIN Order o ON o.user = u"
                        + where
                        + " GROUP BY u.id, u.name, u.email, u.phone, u.userRole, u.emailVerified, u.password, u.createdAt"
                        + buildOrderBy(pageable.getSort()),
                Tuple.class);
        parameters.forEach(query::setParameter);
        query.setParameter("delivered", OrderStatus.DELIVERED);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        var countQuery = entityManager.createQuery("SELECT COUNT(u) FROM User u" + where, Long.class);
        parameters.forEach(countQuery::setParameter);

        var content = query.getResultList().stream().map(this::toResponse).toList();
        return new PageImpl<>(content, pageable, countQuery.getSingleResult());
    }

    private String buildWhere(AdminUserFilter filter, Map<String, Object> parameters) {
        var conditions = new ArrayList<String>();
        if (filter.search() != null && !filter.search().isBlank()) {
            conditions.add("(LOWER(u.email) LIKE :pattern ESCAPE '\\' OR LOWER(u.name) LIKE :pattern ESCAPE '\\')");
            parameters.put("pattern", LikePattern.containsIgnoreCase(filter.search().strip()));
        }
        if (filter.role() != null) {
            conditions.add("u.userRole = :role");
            parameters.put("role", filter.role());
        }
        if (filter.emailVerified() != null) {
            conditions.add("u.emailVerified = :emailVerified");
            parameters.put("emailVerified", filter.emailVerified());
        }
        return conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
    }

    private String buildOrderBy(Sort sort) {
        List<String> orders = new ArrayList<>();
        for (Sort.Order order : sort) {
            String expression = SORT_EXPRESSIONS.get(order.getProperty());
            if (expression != null) {
                orders.add(expression + (order.isAscending() ? " ASC" : " DESC"));
            }
        }
        orders.add("u.id ASC");
        return " ORDER BY " + String.join(", ", orders);
    }

    private AdminUserResponse toResponse(Tuple row) {
        return new AdminUserResponse(
                row.get("id", Long.class),
                row.get("name", String.class),
                row.get("email", String.class),
                row.get("phone", String.class),
                row.get("userRole", UserRole.class),
                row.get("emailVerified", Boolean.class),
                row.get("hasPassword", Boolean.class),
                row.get("createdAt", LocalDateTime.class),
                row.get("ordersCount", Long.class),
                toBigDecimal(row.get("totalSpent")));
    }

    private BigDecimal toBigDecimal(Object value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }
}
