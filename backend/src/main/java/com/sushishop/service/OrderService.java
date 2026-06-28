package com.sushishop.service;

import com.sushishop.domain.Order;
import com.sushishop.domain.OrderItem;
import com.sushishop.domain.enums.DeliveryMethod;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.dto.request.CreateOrderRequest;
import com.sushishop.dto.request.OrderItemRequest;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.OrderMapper;
import com.sushishop.repository.OrderRepository;
import com.sushishop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        validateDelivery(request);
        var items = createOrderItems(request.items());
        var totalAmount = calculateTotalPrice(items);
        var order = buildOrder(request, items, totalAmount);
        items.forEach(i -> i.setOrder(order));
        var saved = orderRepository.save(order);
        log.info("Order created: {}", saved);
        return orderMapper.toResponse(saved);
    }

    public List<OrderResponse> getAll() {
        log.info("Getting all orders");
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    public OrderResponse getById(Long id) {
        log.info("Getting order by id: {}", id);
        return orderRepository.findById(id).map(orderMapper::toResponse).orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    public OrderResponse updateStatus(Long id, OrderStatus status) {
        var order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found: " + id));
        order.setStatus(status);
        var updated = orderRepository.save(order);
        log.info("Order {} status updated to {}", id, status);
        return orderMapper.toResponse(updated);
    }

    private void validateDelivery(CreateOrderRequest request) {
        if (request.deliveryMethod() == DeliveryMethod.DELIVERY && request.address() == null) {
            throw new BadRequestException("Address is required for delivery");
        }
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequest> items) {
        return items.stream().map(item -> {
            var product = productRepository.findById(item.productId()).orElseThrow(() -> new NotFoundException("Product not found: " + item.productId()));
            return OrderItem.builder().product(product).quantity(item.quantity()).price(product.getPrice()).build();
        }).toList();
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> items) {
        return items.stream().map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildOrder(CreateOrderRequest request, List<OrderItem> items, BigDecimal totalPrice) {
        return Order.builder().customerName(request.customerName())
                .phone(request.phone())
                .city(request.address() != null ? request.address().city() : null)
                .street(request.address() != null ? request.address().street() : null)
                .house(request.address() != null ? request.address().house() : null)
                .apartment(request.address() != null ? request.address().apartment() : null)
                .addressComment(request.address() != null ? request.address().comment() : null)
                .deliveryMethod(request.deliveryMethod())
                .totalAmount(totalPrice)
                .items(items)
                .build();
    }
}
