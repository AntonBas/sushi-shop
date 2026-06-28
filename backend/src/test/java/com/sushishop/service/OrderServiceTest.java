package com.sushishop.service;

import com.sushishop.domain.Order;
import com.sushishop.domain.Product;
import com.sushishop.domain.enums.Category;
import com.sushishop.domain.enums.DeliveryMethod;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.dto.request.AddressRequest;
import com.sushishop.dto.request.CreateOrderRequest;
import com.sushishop.dto.request.OrderItemRequest;
import com.sushishop.dto.response.AddressResponse;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.dto.response.OrderStatusUpdateResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.OrderMapper;
import com.sushishop.repository.OrderRepository;
import com.sushishop.repository.ProductRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldCreateOrder() {
        var address = new AddressRequest("Lviv", "Zelena", "204", "280", "code 123");
        var itemRequest = new OrderItemRequest(1L, 2);
        var request = new CreateOrderRequest("Anton", "+380961791111", DeliveryMethod.DELIVERY, address, List.of(itemRequest));

        var product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00")).category(Category.ROLL).build();
        var order = new Order();
        var expectedResponse = new OrderResponse(1L, "Anton", "+380961791111",
                new AddressResponse("Lviv", "Zelena", "204", "280", "code 123"),
                "DELIVERY", "NEW", new BigDecimal("500.00"), null, List.of());

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toResponse(any())).thenReturn(expectedResponse);

        var result = orderService.create(request);

        assertThat(result.customerName()).isEqualTo("Anton");
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(orderRepository).save(any());
    }

    @Test
    void shouldThrowWhenDeliveryWithoutAddress() {
        var request = new CreateOrderRequest("Anton", "+380961791111", DeliveryMethod.DELIVERY, null, List.of());

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldGetById() {
        var order = new Order();
        var expected = new OrderResponse(1L, "Anton", "+380961791111", null, "PICKUP", "NEW", BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldUpdateStatus() {
        var order = new Order();
        var expected = new OrderResponse(1L, "Anton", "+380961791111", null, "PICKUP", "COOKING", BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.updateStatus(1L, OrderStatus.COOKING);

        assertThat(result.status()).isEqualTo("COOKING");
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/1"), any(OrderStatusUpdateResponse.class));
    }
}