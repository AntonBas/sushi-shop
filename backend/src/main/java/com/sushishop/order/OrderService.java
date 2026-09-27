package com.sushishop.order;

import com.sushishop.audit.Auditable;
import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.OrderStatusUpdateResponse;
import com.sushishop.shared.enums.AuditAction;
import com.sushishop.shared.event.OrderCancelledEvent;
import com.sushishop.shared.event.PaymentConfirmedEvent;
import com.sushishop.shared.exception.core.BadRequestException;
import com.sushishop.shared.exception.core.NotFoundException;
import com.sushishop.shared.service.TransactionCallbacks;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id, String requesterEmail, boolean isStaff) {
        log.info("Getting order by id: {}", id);
        return orderMapper.toResponse(getOwnedOrder(id, requesterEmail, isStaff));
    }

    /**
     * Fetches an order by id without any ownership check. For internal/system
     * use only (e.g. payment creation right after {@link #getOwnedOrder} has
     * already verified access, or webhook-driven flows with no requester).
     * User-facing endpoints must go through {@link #getOwnedOrder} instead.
     */
    @Transactional(readOnly = true)
    public Order getOrderByIdInternal(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    @Transactional(readOnly = true)
    public Order getOwnedOrder(Long id, String requesterEmail, boolean isStaff) {
        var order = getOrderByIdInternal(id);
        if (!isStaff && !order.getUser().getEmail().equals(requesterEmail)) {
            throw new NotFoundException("Order not found: " + id);
        }
        return order;
    }

    @Auditable(action = AuditAction.UPDATE, entity = "Order")
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus) {
        var order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));

        order.getStatus().validateTransition(newStatus, order.getDeliveryMethod());
        if (newStatus == OrderStatus.CONFIRMED && order.getPaymentMethod() == PaymentMethod.ONLINE && !order.isPaid()) {
            throw new BadRequestException("Online order cannot be confirmed before it is paid");
        }
        order.setStatus(newStatus);
        var updated = orderRepository.save(order);

        publishStatusUpdate(updated);
        if (newStatus == OrderStatus.CANCELLED && order.getPaymentMethod() == PaymentMethod.ONLINE && !order.isPaid()) {
            eventPublisher.publishEvent(new OrderCancelledEvent(this, id));
        }

        log.info("Order {} status updated to {}", id, newStatus);
        return orderMapper.toResponse(updated);
    }

    @EventListener
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        confirmOrder(event.getOrderId());
    }

    @Transactional
    public void confirmOrder(Long orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        order.setPaid(true);
        if (order.getStatus() != OrderStatus.NEW) {
            orderRepository.save(order);
            if (order.getStatus() == OrderStatus.CANCELLED) {
                log.error("Payment received for cancelled order {}, manual refund required", orderId);
            } else {
                log.warn("Payment received for order {} in status {}, status left unchanged", orderId, order.getStatus());
            }
            return;
        }
        order.setStatus(OrderStatus.CONFIRMED);
        var updated = orderRepository.save(order);
        publishStatusUpdate(updated);
        log.info("Order {} confirmed after payment", orderId);
    }

    private void publishStatusUpdate(Order order) {
        var update = new OrderStatusUpdateResponse(order.getId(), order.getStatus().name(), orderMapper.getPaymentStatus(order));
        TransactionCallbacks.afterCommit(() -> messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), update));
    }
}
