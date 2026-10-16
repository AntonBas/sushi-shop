package com.sushishop.admin;

import com.sushishop.admin.dto.AdminUserResponse;
import com.sushishop.order.DeliveryMethod;
import com.sushishop.order.Order;
import com.sushishop.order.OrderRepository;
import com.sushishop.order.OrderStatus;
import com.sushishop.order.PaymentMethod;
import com.sushishop.user.User;
import com.sushishop.user.UserRepository;
import com.sushishop.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AdminUserQueryRepositoryTest {

    private static final String SEARCH = "adminquery";

    @Autowired
    private AdminUserQueryRepository adminUserQueryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        var anna = saveUser("Anna", "anna.adminquery@example.com", UserRole.CUSTOMER, true, "hash");
        var bohdan = saveUser("Bohdan", "bohdan.adminquery@example.com", UserRole.COURIER, false, null);
        saveUser("Cyril", "cyril.adminquery@example.com", UserRole.CUSTOMER, true, "hash");

        saveOrder(anna, OrderStatus.DELIVERED, "300.00");
        saveOrder(anna, OrderStatus.DELIVERED, "200.00");
        saveOrder(anna, OrderStatus.CANCELLED, "999.00");
        saveOrder(bohdan, OrderStatus.DELIVERED, "100.00");
    }

    @Test
    void shouldAggregateOrdersPerUserCountingOnlyDeliveredSpending() {
        var page = adminUserQueryRepository.search(filter(null, null),
                PageRequest.of(0, 10, Sort.by("email")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        var anna = page.getContent().getFirst();
        assertThat(anna.email()).isEqualTo("anna.adminquery@example.com");
        assertThat(anna.ordersCount()).isEqualTo(3);
        assertThat(anna.totalSpent()).isEqualByComparingTo("500.00");
        assertThat(anna.hasPassword()).isTrue();
        var cyril = page.getContent().get(2);
        assertThat(cyril.ordersCount()).isZero();
        assertThat(cyril.totalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldSortByTotalSpentDescending() {
        var page = adminUserQueryRepository.search(filter(null, null),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "totalSpent")));

        assertThat(page.getContent()).extracting(AdminUserResponse::name)
                .containsExactly("Anna", "Bohdan", "Cyril");
    }

    @Test
    void shouldFilterByRoleAndVerification() {
        var couriers = adminUserQueryRepository.search(filter(UserRole.COURIER, null), PageRequest.of(0, 10));
        var unverified = adminUserQueryRepository.search(filter(null, false), PageRequest.of(0, 10));

        assertThat(couriers.getContent()).extracting(AdminUserResponse::name).containsExactly("Bohdan");
        assertThat(couriers.getContent().getFirst().hasPassword()).isFalse();
        assertThat(unverified.getContent()).extracting(AdminUserResponse::name).containsExactly("Bohdan");
    }

    @Test
    void shouldSearchByNameCaseInsensitively() {
        var page = adminUserQueryRepository.search(new AdminUserFilter("CYRIL", null, null), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(AdminUserResponse::email)
                .containsExactly("cyril.adminquery@example.com");
    }

    @Test
    void shouldPaginateWithTotalCount() {
        var page = adminUserQueryRepository.search(filter(null, null),
                PageRequest.of(1, 2, Sort.by("email")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).extracting(AdminUserResponse::name).containsExactly("Cyril");
    }

    private AdminUserFilter filter(UserRole role, Boolean emailVerified) {
        return new AdminUserFilter(SEARCH, role, emailVerified);
    }

    private User saveUser(String name, String email, UserRole role, boolean verified, String password) {
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .userRole(role)
                .emailVerified(verified)
                .password(password == null ? null : password.repeat(4))
                .build());
    }

    private void saveOrder(User user, OrderStatus status, String total) {
        orderRepository.save(Order.builder()
                .user(user)
                .customerName(user.getName())
                .phone("+380991234567")
                .status(status)
                .paymentMethod(PaymentMethod.ON_DELIVERY)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .totalAmount(new BigDecimal(total))
                .build());
    }
}
