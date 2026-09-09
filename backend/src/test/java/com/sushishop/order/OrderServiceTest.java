package com.sushishop.order;

import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.OrderStatusUpdateResponse;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    public void shouldGetByIdForOwner() {
        var order = new Order();
        order.setUser(User.builder().email("anton@example.com").build());
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW,
                BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.getById(1L, "anton@example.com", false);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    public void shouldGetByIdForStaffRegardlessOfOwner() {
        var order = new Order();
        order.setUser(User.builder().email("anton@example.com").build());
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW,
                BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.getById(1L, "admin@example.com", true);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    public void shouldThrowNotFoundWhenRequesterIsNotOwnerOrStaff() {
        var order = new Order();
        order.setUser(User.builder().email("anton@example.com").build());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getById(1L, "stranger@example.com", false))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldUpdateStatusForDelivery() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.NEW)
                .deliveryMethod(DeliveryMethod.DELIVERY)
                .build();
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.CONFIRMED,
                BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/1"), any(OrderStatusUpdateResponse.class));
    }

    @Test
    public void shouldUpdateStatusToReadyForPickup() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.COOKING)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .build();
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.READY,
                BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.updateStatus(1L, OrderStatus.READY);

        assertThat(result.status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    public void shouldUpdateStatusToDeliveredForPickup() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.READY)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .build();
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null,
                DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.DELIVERED,
                BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.updateStatus(1L, OrderStatus.DELIVERED);

        assertThat(result.status()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    public void shouldThrowWhenDeliveringForPickup() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.COOKING)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.DELIVERING))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot set DELIVERING for PICKUP order");
    }

    @Test
    public void shouldThrowWhenReadyForDelivery() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.COOKING)
                .deliveryMethod(DeliveryMethod.DELIVERY)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.READY))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot set READY for DELIVERY order");
    }

    @Test
    public void shouldThrowWhenSameStatus() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.NEW)
                .deliveryMethod(DeliveryMethod.PICKUP)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.NEW))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order already has status");
    }

    @Test
    public void shouldThrowWhenCancelNonNewOrder() {
        var order = Order.builder()
                .id(1L)
                .status(OrderStatus.COOKING)
                .deliveryMethod(DeliveryMethod.DELIVERY)
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot cancel order with status");
    }
}