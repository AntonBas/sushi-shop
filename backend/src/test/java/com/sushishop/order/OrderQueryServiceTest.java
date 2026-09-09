package com.sushishop.order;

import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderQueryServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderQueryService orderQueryService;

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetAllOrders() {
        Pageable pageable = PageRequest.of(0, 20);
        var order = Order.builder().id(1L).customerName("Anton").totalAmount(new BigDecimal("500.00")).build();
        var page = new PageImpl<>(List.of(order), pageable, 1);
        var expectedResponse = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW,
                new BigDecimal("500.00"), null, List.of());

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(orderRepository.findItemsByOrderIds(anyList())).thenReturn(List.of());
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        var result = orderQueryService.getAll(pageable, null, null, null, null);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().customerName()).isEqualTo("Anton");
        verify(orderRepository).findItemsByOrderIds(anyList());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetAllOrdersWithFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        var order = Order.builder().id(1L).status(OrderStatus.NEW).build();
        var page = new PageImpl<>(List.of(order), pageable, 1);
        var expectedResponse = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW,
                BigDecimal.ZERO, null, List.of());

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(orderRepository.findItemsByOrderIds(anyList())).thenReturn(List.of());
        when(orderMapper.toResponse(order)).thenReturn(expectedResponse);

        var result = orderQueryService.getAll(pageable, OrderStatus.NEW, DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "Anton");

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findItemsByOrderIds(anyList());
    }

    @Test
    public void shouldReturnEmptyPageWhenNoOrders() {
        Pageable pageable = PageRequest.of(0, 20);
        var page = new PageImpl<Order>(List.of(), pageable, 0);

        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        var result = orderQueryService.getAll(pageable, null, null, null, null);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    public void shouldGetByUser() {
        Pageable pageable = PageRequest.of(0, 20);
        var order = Order.builder().id(1L).customerName("Anton").build();
        var page = new PageImpl<>(List.of(order), pageable, 1);
        var expectedResponse = new UserOrderResponse(1L, "test@test.com", OrderStatus.NEW, DeliveryMethod.DELIVERY,
                PaymentMethod.ON_DELIVERY, "ON_DELIVERY", BigDecimal.ZERO, null, List.of());

        when(orderRepository.findByUserEmail("test@test.com", pageable)).thenReturn(page);
        when(orderRepository.findItemsByOrderIds(anyList())).thenReturn(List.of());
        when(orderMapper.toUserResponse(order)).thenReturn(expectedResponse);

        var result = orderQueryService.getByUser("test@test.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().userEmail()).isEqualTo("test@test.com");
        verify(orderRepository).findItemsByOrderIds(anyList());
    }
}