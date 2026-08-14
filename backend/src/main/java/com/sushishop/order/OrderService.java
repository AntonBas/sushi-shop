package com.sushishop.order;

import com.sushishop.audit.Auditable;
import com.sushishop.order.dto.request.CreateOrderRequest;
import com.sushishop.order.dto.request.OrderItemRequest;
import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.OrderStatusUpdateResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import com.sushishop.product.ProductRepository;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.enums.DeliveryMethod;
import com.sushishop.shared.enums.OrderStatus;
import com.sushishop.shared.enums.PaymentMethod;
import com.sushishop.payment.PaymentConfirmedEvent;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Auditable(action = AuditAction.CREATE, entity = "Order")
    @Transactional
    public OrderResponse create(CreateOrderRequest request, String userEmail) {
        var user = userRepository.findByEmail(userEmail).orElseThrow(() -> new NotFoundException("User not found"));
        validateDelivery(request);
        var items = createOrderItems(request.items());
        var totalAmount = calculateTotalAmount(items);
        var order = buildOrder(request, items, totalAmount);
        order.setUser(user);
        items.forEach(i -> i.setOrder(order));

        if (request.paymentMethod() == PaymentMethod.ON_DELIVERY) {
            order.setStatus(OrderStatus.CONFIRMED);
        }

        var saved = orderRepository.save(order);
        messagingTemplate.convertAndSend("/topic/orders/new",
                new OrderStatusUpdateResponse(saved.getId(), saved.getStatus().name()));
        log.info("Order created: {}", saved.getId());
        return orderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAll(Pageable pageable, OrderStatus status,
                                      DeliveryMethod deliveryMethod, PaymentMethod paymentMethod, String search) {
        var spec = Specification.where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.hasDeliveryMethod(deliveryMethod))
                .and(OrderSpecification.hasPaymentMethod(paymentMethod))
                .and(OrderSpecification.hasSearch(search));

        var page = orderRepository.findAll(spec, pageable);
        List<Order> orders = page.getContent();

        if (orders.isEmpty()) {
            return Page.empty(pageable);
        }

        enrichOrdersWithItems(orders);

        List<OrderResponse> responses = orders.stream()
                .map(orderMapper::toResponse)
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    public OrderResponse getById(Long id) {
        log.info("Getting order by id: {}", id);
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<UserOrderResponse> getByUser(String email, Pageable pageable) {
        var page = orderRepository.findByUserEmail(email, pageable);
        List<Order> orders = page.getContent();

        if (orders.isEmpty()) {
            return Page.empty(pageable);
        }

        enrichOrdersWithItems(orders);

        List<UserOrderResponse> responses = orders.stream()
                .map(orderMapper::toUserResponse)
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Order")
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        order.getStatus().validateTransition(newStatus, order.getDeliveryMethod());
        order.setStatus(newStatus);
        var updated = orderRepository.save(order);

        messagingTemplate.convertAndSend("/topic/orders/" + id,
                new OrderStatusUpdateResponse(updated.getId(), updated.getStatus().name()));

        log.info("Order {} status updated to {}", id, newStatus);
        return orderMapper.toResponse(updated);
    }

    @EventListener
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        confirmOrder(event.getPayment().getOrder().getId());
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        log.info("Order {} confirmed after payment", orderId);
    }

    private void enrichOrdersWithItems(List<Order> orders) {
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderRepository.findItemsByOrderIds(orderIds);

        Map<Long, List<OrderItem>> itemsByOrder = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        orders.forEach(order -> {
            List<OrderItem> items = itemsByOrder.getOrDefault(order.getId(), List.of());
            order.setItems(items);
        });
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
            var unitPrice = product.getPrice();
            var subtotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
            return OrderItem.builder()
                    .product(product)
                    .quantity(item.quantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order buildOrder(CreateOrderRequest request, List<OrderItem> items, BigDecimal totalAmount) {
        return Order.builder()
                .customerName(request.customerName())
                .phone(request.phone())
                .paymentMethod(request.paymentMethod())
                .city(request.address() != null ? request.address().city() : null)
                .street(request.address() != null ? request.address().street() : null)
                .house(request.address() != null ? request.address().house() : null)
                .apartment(request.address() != null ? request.address().apartment() : null)
                .addressComment(request.address() != null ? request.address().comment() : null)
                .deliveryMethod(request.deliveryMethod())
                .totalAmount(totalAmount)
                .items(items)
                .build();
    }
}