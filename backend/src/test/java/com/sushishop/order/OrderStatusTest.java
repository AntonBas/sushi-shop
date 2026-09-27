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
        assertThatCode(() -> OrderStatus.NEW.validateTransition(OrderStatus.CANCELLED, DeliveryMethod.PICKUP, PaymentMethod.ONLINE))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"CONFIRMED", "COOKING", "READY", "DELIVERING", "DELIVERED"})
    void shouldRejectCancellingOrderPastNew(OrderStatus status) {
        assertThatThrownBy(() -> status.validateTransition(OrderStatus.CANCELLED, DeliveryMethod.DELIVERY, PaymentMethod.ONLINE))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldFollowPickupFlow() {
        assertThatCode(() -> {
            OrderStatus.NEW.validateTransition(OrderStatus.CONFIRMED, DeliveryMethod.PICKUP, PaymentMethod.ONLINE);
            OrderStatus.CONFIRMED.validateTransition(OrderStatus.COOKING, DeliveryMethod.PICKUP, PaymentMethod.ONLINE);
            OrderStatus.COOKING.validateTransition(OrderStatus.READY, DeliveryMethod.PICKUP, PaymentMethod.ONLINE);
            OrderStatus.READY.validateTransition(OrderStatus.DELIVERED, DeliveryMethod.PICKUP, PaymentMethod.ONLINE);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldFollowDeliveryFlow() {
        assertThatCode(() -> {
            OrderStatus.CONFIRMED.validateTransition(OrderStatus.COOKING, DeliveryMethod.DELIVERY, PaymentMethod.ONLINE);
            OrderStatus.COOKING.validateTransition(OrderStatus.DELIVERING, DeliveryMethod.DELIVERY, PaymentMethod.ONLINE);
            OrderStatus.DELIVERING.validateTransition(OrderStatus.DELIVERED, DeliveryMethod.DELIVERY, PaymentMethod.ONLINE);
        }).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectSkippingSteps() {
        assertThatThrownBy(() -> OrderStatus.NEW.validateTransition(OrderStatus.COOKING, DeliveryMethod.PICKUP, PaymentMethod.ONLINE))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> OrderStatus.COOKING.validateTransition(OrderStatus.DELIVERING, DeliveryMethod.PICKUP, PaymentMethod.ONLINE))
                .isInstanceOf(BadRequestException.class);
    }
}
