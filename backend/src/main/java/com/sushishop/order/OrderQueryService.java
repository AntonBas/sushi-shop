package com.sushishop.order;

import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import com.sushishop.shared.enums.DeliveryMethod;
import com.sushishop.shared.enums.OrderStatus;
import com.sushishop.shared.enums.PaymentMethod;
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

        enrichOrdersWithItems(orders);

        List<OrderResponse> responses = orders.stream()
                .map(orderMapper::toResponse)
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

        enrichOrdersWithItems(orders);

        List<UserOrderResponse> responses = orders.stream()
                .map(orderMapper::toUserResponse)
                .toList();

        return new PageImpl<>(responses, pageable, page.getTotalElements());
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
}