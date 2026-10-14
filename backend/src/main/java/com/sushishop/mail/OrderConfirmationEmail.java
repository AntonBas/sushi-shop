package com.sushishop.mail;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmationEmail(
        Long orderId,
        String customerName,
        List<Line> lines,
        BigDecimal totalAmount,
        String deliveryDetails,
        boolean paidOnline
) {

    public record Line(String productName, int quantity, BigDecimal subtotal) {
    }
}
