package com.sushishop.order;

import com.sushishop.mail.MailService;
import com.sushishop.mail.OrderConfirmationEmail;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class OrderConfirmationNotifier {

    private final MailService mailService;

    public void notifyCustomer(Order order) {
        var lines = order.getItems().stream()
                .map(item -> new OrderConfirmationEmail.Line(item.getProduct().getName(), item.getQuantity(), item.getSubtotal()))
                .toList();
        var email = new OrderConfirmationEmail(order.getId(), order.getCustomerName(), lines, order.getTotalAmount(),
                deliveryDetails(order), order.getPaymentMethod() == PaymentMethod.ONLINE);
        mailService.sendOrderConfirmation(order.getUser().getEmail(), email);
    }

    private String deliveryDetails(Order order) {
        if (order.getDeliveryMethod() == DeliveryMethod.PICKUP) {
            return "Pickup from the restaurant";
        }
        String address = Stream.of(order.getCity(), order.getStreet(), order.getHouse())
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(", "));
        return "Delivery to " + address
                + (order.getApartment() != null && !order.getApartment().isBlank() ? ", apt. " + order.getApartment() : "");
    }
}
