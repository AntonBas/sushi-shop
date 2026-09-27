package com.sushishop.order;

import com.sushishop.shared.exception.core.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTest {

    @Test
    void shouldAllowCancellingNewOrder() {
        assertThatCode(() -> OrderStatus.NEW.validateTransition(OrderStatus.CANCELLED, DeliveryMethod.PICKUP))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "COOKING", "READY", "DELIVERING", "DELIVERED"})
    void shouldRejectCancellingOrderPastNew(OrderStatus status) {
        assertThatThrownBy(() -> status.validateTransition(OrderStatus.CANCELLED, DeliveryMethod.DELIVERY))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldFollowPickupFlow() {
        assertThatCode(() -> {
            OrderStatus.NEW.validateTransition(OrderStatus.CONFIRMED, DeliveryMethod.PICKUP);
            OrderStatus.CONFIRMED.validateTransition(OrderStatus.COOKING, DeliveryMethod.PICKUP);
            OrderStatus.COOKING.validateTransition(OrderStatus.READY, DeliveryMethod.PICKUP);
            OrderStatus.READY.validateTransition(OrderStatus.DELIVERED, DeliveryMethod.PICKUP);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldFollowDeliveryFlow() {
        assertThatCode(() -> {
            OrderStatus.CONFIRMED.validateTransition(OrderStatus.COOKING, DeliveryMethod.DELIVERY);
            OrderStatus.COOKING.validateTransition(OrderStatus.DELIVERING, DeliveryMethod.DELIVERY);
            OrderStatus.DELIVERING.validateTransition(OrderStatus.DELIVERED, DeliveryMethod.DELIVERY);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSkippingSteps() {
        assertThatThrownBy(() -> OrderStatus.NEW.validateTransition(OrderStatus.COOKING, DeliveryMethod.PICKUP))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> OrderStatus.COOKING.validateTransition(OrderStatus.DELIVERING, DeliveryMethod.PICKUP))
                .isInstanceOf(BadRequestException.class);
    }
}
