package com.sushishop.order;

import com.sushishop.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@ActiveProfiles("testcontainers")
class OrderRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldRejectStaleOrderUpdateInsteadOfOverwritingPaidFlag() {
        var user = userRepository.findByEmail("user@test.com").orElseThrow();
        var saved = orderRepository.save(Order.builder()
                .user(user)
                .customerName("Anton")
                .phone("+380961791111")
                .paymentMethod(PaymentMethod.ONLINE)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .totalAmount(BigDecimal.TEN)
                .build());

        var webhookCopy = orderRepository.findById(saved.getId()).orElseThrow();
        var adminCopy = orderRepository.findById(saved.getId()).orElseThrow();

        webhookCopy.setPaid(true);
        webhookCopy.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(webhookCopy);

        adminCopy.setStatus(OrderStatus.CANCELLED);
        assertThatThrownBy(() -> orderRepository.save(adminCopy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        var reloaded = orderRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.isPaid()).isTrue();
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
