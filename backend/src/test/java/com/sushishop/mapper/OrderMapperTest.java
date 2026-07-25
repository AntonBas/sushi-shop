package com.sushishop.mapper;

import com.sushishop.domain.Order;
import com.sushishop.domain.OrderItem;
import com.sushishop.domain.Product;
import com.sushishop.domain.enums.DeliveryMethod;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.dto.response.OrderItemResponse;
import com.sushishop.dto.response.OrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class OrderMapperTest {
    @Autowired
    private OrderMapper orderMapper;

    @Test
    void shouldMapToResponse() {
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("250.00"))
                .build();

        Order order = Order.builder()
                .id(1L)
                .customerName("Anton")
                .phone("+380961791111")
                .city("Lviv")
                .street("Zelena")
                .house("204")
                .apartment("280")
                .addressComment("code 123")
                .deliveryMethod(DeliveryMethod.DELIVERY)
                .status(OrderStatus.NEW)
                .totalAmount(new BigDecimal("500.00"))
                .items(List.of(item))
                .build();

        item.setOrder(order);

        OrderResponse response = orderMapper.toResponse(order);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.customerName()).isEqualTo("Anton");
        assertThat(response.phone()).isEqualTo("+380961791111");
        assertThat(response.deliveryMethod()).isEqualTo(DeliveryMethod.DELIVERY);
        assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.address()).isNotNull();
        assertThat(response.address().city()).isEqualTo("Lviv");
        assertThat(response.address().street()).isEqualTo("Zelena");
        assertThat(response.address().house()).isEqualTo("204");
        assertThat(response.address().apartment()).isEqualTo("280");
        assertThat(response.address().comment()).isEqualTo("code 123");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void shouldMapToItemResponse() {
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .price(new BigDecimal("250.00"))
                .build();

        OrderItem item = OrderItem.builder()
                .id(1L)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("250.00"))
                .build();

        OrderItemResponse response = orderMapper.toItemResponse(item);

        assertThat(response.productId()).isEqualTo(1L);
        assertThat(response.productName()).isEqualTo("Maki");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldMapToItemResponseList() {
        Product product = Product.builder()
                .id(1L)
                .name("Maki")
                .build();

        OrderItem item1 = OrderItem.builder()
                .product(product)
                .quantity(2)
                .price(new BigDecimal("250.00"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .product(product)
                .quantity(1)
                .price(new BigDecimal("250.00"))
                .build();

        List<OrderItemResponse> responses = orderMapper.toItemResponseList(List.of(item1, item2));

        assertThat(responses).hasSize(2);
    }
}