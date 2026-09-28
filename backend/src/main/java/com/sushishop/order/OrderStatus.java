package com.sushishop.order;

import com.sushishop.shared.exception.core.BadRequestException;

public enum OrderStatus {
    NEW, CONFIRMED, COOKING, DELIVERING, DELIVERED, READY, CANCELLED;

    public void validateTransition(OrderStatus newStatus, DeliveryMethod deliveryMethod, PaymentMethod paymentMethod) {
        if (deliveryMethod == null) {
            throw new BadRequestException("Delivery method is required");
        }

        if (this == newStatus) {
            throw new BadRequestException("Order already has status: " + this);
        }

        if (newStatus == CANCELLED && !isCancellable(paymentMethod)) {
            throw new BadRequestException("Cannot cancel order with status: " + this);
        }

        if (newStatus == DELIVERING && deliveryMethod == DeliveryMethod.PICKUP) {
            throw new BadRequestException("Cannot set DELIVERING for PICKUP order");
        }

        if (newStatus == READY && deliveryMethod == DeliveryMethod.DELIVERY) {
            throw new BadRequestException("Cannot set READY for DELIVERY order");
        }

        if (newStatus == DELIVERED && this != READY && this != DELIVERING) {
            throw new BadRequestException("Cannot set DELIVERED from status: " + this);
        }

        if (!isValidTransition(newStatus)) {
            throw new BadRequestException("Cannot transition from " + this + " to " + newStatus);
        }
    }

    private boolean isCancellable(PaymentMethod paymentMethod) {
        return this == NEW || (this == CONFIRMED && paymentMethod == PaymentMethod.ON_DELIVERY);
    }

    private boolean isValidTransition(OrderStatus newStatus) {
        return switch (this) {
            case NEW -> newStatus == CONFIRMED || newStatus == CANCELLED;
            case CONFIRMED -> newStatus == COOKING || newStatus == CANCELLED;
            case COOKING -> newStatus == READY || newStatus == DELIVERING;
            case DELIVERING, READY -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}