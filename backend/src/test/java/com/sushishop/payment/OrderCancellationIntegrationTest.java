package com.sushishop.payment;

import com.sushishop.order.OrderService;
import com.sushishop.order.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class OrderCancellationIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockitoBean
    private StripeService stripeService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void shouldExpirePendingPaymentAfterOnlineOrderIsCancelled() {
        var userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = 'user@test.com'", Long.class);
        jdbcTemplate.update("""
                INSERT INTO orders (user_id, customer_name, phone, status, payment_method, delivery_method,
                                    total_amount, created_at)
                VALUES (?, 'Anton', '+380961791111', 'NEW', 'ONLINE', 'PICKUP', 100, NOW())
                """, userId);
        var orderId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM orders", Long.class);
        jdbcTemplate.update("""
                INSERT INTO payments (stripe_session_id, order_id, amount, status)
                VALUES ('cs_cancel_1', ?, 100, 'PENDING')
                """, orderId);

        orderService.updateStatus(orderId, OrderStatus.CANCELLED);

        var status = jdbcTemplate.queryForObject(
                "SELECT status FROM payments WHERE stripe_session_id = 'cs_cancel_1'", String.class);
        assertThat(status).isEqualTo("EXPIRED");
        verify(stripeService).expireCheckoutSession("cs_cancel_1");
    }
}
