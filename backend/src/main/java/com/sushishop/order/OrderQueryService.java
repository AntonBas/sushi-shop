package com.sushishop.order;

import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

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

        var itemsByOrder = loadItemsByOrderId(orders);

        List<OrderResponse> responses = orders.stream()
                .map(order -> orderMapper.toResponse(order, itemsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<UserOrderResponse> getByUser(String email, Pageable pageable) {
        var page = orderRepository.findByUserEmail(email, pageable);
        List<Order> orders = page.getContent();

        if (orders.isEmpty()) {
            return Page.empty(pageable);
        }

        var itemsByOrder = loadItemsByOrderId(orders);

        List<UserOrderResponse> responses = orders.stream()
                .map(order -> orderMapper.toUserResponse(order, itemsByOrder.getOrDefault(order.getId(), List.of())))
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
    }

    private Map<Long, List<OrderItem>> loadItemsByOrderId(List<Order> orders) {
        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        return orderRepository.findItemsByOrderIds(orderIds).stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));
    }
}
