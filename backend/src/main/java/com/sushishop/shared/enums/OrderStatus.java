package com.sushishop.shared.enums;

import com.sushishop.shared.exception.core.BadRequestException;

public enum OrderStatus {
    NEW, CONFIRMED, COOKING, DELIVERING, DELIVERED, READY, CANCELLED;

    public void validateTransition(OrderStatus newStatus, DeliveryMethod deliveryMethod) {
        if (this == newStatus) {
            throw new BadRequestException("Order already has status: " + this);
        }

        if (newStatus == CANCELLED && this != NEW) {
            throw new BadRequestException("Cannot cancel order with status: " + this);
        }

        if (newStatus == DELIVERING && deliveryMethod == DeliveryMethod.PICKUP) {
            throw new BadRequestException("Cannot set DELIVERING for PICKUP order");
        }

        if (newStatus == READY && deliveryMethod == DeliveryMethod.DELIVERY) {
            throw new BadRequestException("Cannot set READY for DELIVERY order");
        }

        if (newStatus == DELIVERED && deliveryMethod != DeliveryMethod.DELIVERY) {
            throw new BadRequestException("Cannot set DELIVERED for PICKUP order");
        }
    }
}