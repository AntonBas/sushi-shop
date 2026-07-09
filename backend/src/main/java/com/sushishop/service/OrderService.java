package com.sushishop.service;

import com.sushishop.domain.Order;
import com.sushishop.domain.OrderItem;
import com.sushishop.domain.enums.DeliveryMethod;
import com.sushishop.domain.enums.OrderStatus;
import com.sushishop.dto.request.CreateOrderRequest;
import com.sushishop.dto.request.OrderItemRequest;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.dto.response.OrderStatusUpdateResponse;
import com.sushishop.exception.core.BadRequestException;
import com.sushishop.exception.core.NotFoundException;
import com.sushishop.mapper.OrderMapper;
import com.sushishop.repository.OrderRepository;
import com.sushishop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        validateDelivery(request);
        var items = createOrderItems(request.items());
        var totalAmount = calculateTotalPrice(items);
        var order = buildOrder(request, items, totalAmount);
        items.forEach(i -> i.setOrder(order));
        var saved = orderRepository.save(order);
        log.info("Order created: {}", saved.getId());
        return orderMapper.toResponse(saved);
    }

    public Page<OrderResponse> getAll(Pageable pageable) {
        log.info("Getting all orders");
        return orderRepository.findAll(pageable).map(orderMapper::toResponse);
    }

    public OrderResponse getById(Long id) {
        log.info("Getting order by id: {}", id);
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        validateStatusTransition(order, newStatus);
        order.setStatus(newStatus);
        var updated = orderRepository.save(order);

        messagingTemplate.convertAndSend("/topic/orders/" + id,
                new OrderStatusUpdateResponse(updated.getId(), updated.getStatus().name()));

        log.info("Order {} status updated to {}", id, newStatus);
        return orderMapper.toResponse(updated);
    }

    private void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus current = order.getStatus();

        if (current == newStatus) {
            throw new BadRequestException("Order already has status: " + current);
        }

        if (newStatus == OrderStatus.CANCELLED && current != OrderStatus.NEW) {
            throw new BadRequestException("Cannot cancel order with status: " + current);
        }

        if (newStatus == OrderStatus.DELIVERING && order.getDeliveryMethod() == DeliveryMethod.PICKUP) {
            throw new BadRequestException("Cannot set DELIVERING for PICKUP order");
        }

        if (newStatus == OrderStatus.READY && order.getDeliveryMethod() == DeliveryMethod.DELIVERY) {
            throw new BadRequestException("Cannot set READY for DELIVERY order");
        }

        if (newStatus == OrderStatus.DELIVERED && order.getDeliveryMethod() != DeliveryMethod.DELIVERY) {
            throw new BadRequestException("Cannot set DELIVERED for PICKUP order");
        }
    }

    private void validateDelivery(CreateOrderRequest request) {
        if (request.deliveryMethod() == DeliveryMethod.DELIVERY && request.address() == null) {
            throw new BadRequestException("Address is required for delivery");
        }
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequest> items) {
        return items.stream().map(item -> {
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new BadRequestException("Quantity must be positive for product: " + item.productId());
            }
            var product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + item.productId()));
            if (!product.isAvailable()) {
                throw new BadRequestException("Product is not available: " + product.getName());
            }
            return OrderItem.builder()
                    .product(product)
                    .quantity(item.quantity())
                    .price(product.getPrice())
                    .build();
        }).toList();
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> items) {
        return items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildOrder(CreateOrderRequest request, List<OrderItem> items, BigDecimal totalPrice) {
        return Order.builder()
                .customerName(request.customerName())
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