package com.sushishop.order;

import com.sushishop.product.Product;
import com.sushishop.user.User;
import com.sushishop.shared.enums.Category;
import com.sushishop.shared.enums.DeliveryMethod;
import com.sushishop.shared.enums.OrderStatus;
import com.sushishop.shared.enums.PaymentMethod;
import com.sushishop.shared.address.AddressRequest;
import com.sushishop.order.dto.request.CreateOrderRequest;
import com.sushishop.order.dto.request.OrderItemRequest;
import com.sushishop.shared.address.AddressResponse;
import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.OrderStatusUpdateResponse;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.product.ProductRepository;
import com.sushishop.user.UserRepository;
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
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrder() {
        var address = new AddressRequest("Lviv", "Zelena", "204", "280", "code 123");
        var itemRequest = new OrderItemRequest(1L, 2);
        var request = new CreateOrderRequest("Anton", "+380961791111", PaymentMethod.ON_DELIVERY, DeliveryMethod.DELIVERY, address, List.of(itemRequest));

        var user = User.builder().id(1L).email("test@test.com").build();
        var product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00")).category(Category.ROLL).available(true).build();
        var order = new Order();
        var expectedResponse = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111",
                new AddressResponse("Lviv", "Zelena", "204", "280", "code 123"),
                DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW, new BigDecimal("500.00"), null, List.of());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenReturn(order);
        when(orderMapper.toResponse(any())).thenReturn(expectedResponse);

        var result = orderService.create(request, "test@test.com");

        assertThat(result.customerName()).isEqualTo("Anton");
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        verify(orderRepository).save(any());
    }

    @Test
    void shouldThrowWhenProductNotAvailable() {
        var itemRequest = new OrderItemRequest(1L, 2);
        var request = new CreateOrderRequest("Anton", "+380961791111", PaymentMethod.ON_DELIVERY, DeliveryMethod.PICKUP, null, List.of(itemRequest));

        var user = User.builder().id(1L).email("test@test.com").build();
        var product = Product.builder().id(1L).name("Maki").price(new BigDecimal("250.00")).available(false).build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.create(request, "test@test.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Product is not available");
    }

    @Test
    void shouldThrowWhenDeliveryWithoutAddress() {
        var request = new CreateOrderRequest("Anton", "+380961791111", PaymentMethod.ON_DELIVERY, DeliveryMethod.DELIVERY, null, List.of());

        var user = User.builder().id(1L).email("test@test.com").build();
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> orderService.create(request, "test@test.com"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldGetById() {
        var order = new Order();
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null, DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.NEW, BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldUpdateStatusForDelivery() {
        var order = Order.builder().id(1L).status(OrderStatus.NEW).deliveryMethod(DeliveryMethod.DELIVERY).build();
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null, DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.CONFIRMED, BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.updateStatus(1L, OrderStatus.CONFIRMED);

        assertThat(result.status()).isEqualTo(OrderStatus.CONFIRMED);
        verify(messagingTemplate).convertAndSend(eq("/topic/orders/1"), any(OrderStatusUpdateResponse.class));
    }

    @Test
    void shouldUpdateStatusToReadyForPickup() {
        var order = Order.builder().id(1L).status(OrderStatus.COOKING).deliveryMethod(DeliveryMethod.PICKUP).build();
        var expected = new OrderResponse(1L, "Anton", "test@test.com", "+380961791111", null, DeliveryMethod.PICKUP, PaymentMethod.ON_DELIVERY, "ON_DELIVERY", OrderStatus.READY, BigDecimal.ZERO, null, List.of());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(expected);

        var result = orderService.updateStatus(1L, OrderStatus.READY);

        assertThat(result.status()).isEqualTo(OrderStatus.READY);
    }

    @Test
    void shouldThrowWhenDeliveringForPickup() {
        var order = Order.builder().id(1L).status(OrderStatus.COOKING).deliveryMethod(DeliveryMethod.PICKUP).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.DELIVERING))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot set DELIVERING for PICKUP order");
    }

    @Test
    void shouldThrowWhenReadyForDelivery() {
        var order = Order.builder().id(1L).status(OrderStatus.COOKING).deliveryMethod(DeliveryMethod.DELIVERY).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.READY))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot set READY for DELIVERY order");
    }

    @Test
    void shouldThrowWhenDeliveredForPickup() {
        var order = Order.builder().id(1L).status(OrderStatus.DELIVERING).deliveryMethod(DeliveryMethod.PICKUP).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.DELIVERED))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot set DELIVERED for PICKUP order");
    }

    @Test
    void shouldThrowWhenSameStatus() {
        var order = Order.builder().id(1L).status(OrderStatus.NEW).deliveryMethod(DeliveryMethod.PICKUP).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.NEW))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Order already has status");
    }

    @Test
    void shouldThrowWhenCancelNonNewOrder() {
        var order = Order.builder().id(1L).status(OrderStatus.COOKING).deliveryMethod(DeliveryMethod.DELIVERY).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(1L, OrderStatus.CANCELLED))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Cannot cancel order with status");
    }
}