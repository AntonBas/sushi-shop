package com.sushishop.order;

import com.sushishop.mail.MailService;
import com.sushishop.mail.OrderConfirmationEmail;
import com.sushishop.product.Product;
import com.sushishop.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderConfirmationNotifierTest {

    @Mock
    private MailService mailService;

    @InjectMocks
    private OrderConfirmationNotifier notifier;

    @Test
    void shouldEmailOrderLinesAndDeliveryAddressToCustomer() {
        var order = order(DeliveryMethod.DELIVERY, PaymentMethod.ON_DELIVERY);
        order.setCity("Lviv");
        order.setStreet("Zelena");
        order.setHouse("204");
        order.setApartment("28");

        var email = send(order);

        assertThat(email.orderId()).isEqualTo(7L);
        assertThat(email.lines()).containsExactly(new OrderConfirmationEmail.Line("Maki", 2, new BigDecimal("500.00")));
        assertThat(email.totalAmount()).isEqualByComparingTo("500.00");
        assertThat(email.deliveryDetails()).isEqualTo("Delivery to Lviv, Zelena, 204, apt. 28");
        assertThat(email.paidOnline()).isFalse();
    }

    @Test
    void shouldDescribePickupAndOnlinePayment() {
        var email = send(order(DeliveryMethod.PICKUP, PaymentMethod.ONLINE));

        assertThat(email.deliveryDetails()).isEqualTo("Pickup from the restaurant");
        assertThat(email.paidOnline()).isTrue();
    }

    private OrderConfirmationEmail send(Order order) {
        notifier.notifyCustomer(order);
        var captor = ArgumentCaptor.forClass(OrderConfirmationEmail.class);
        verify(mailService).sendOrderConfirmation(eq("anton@example.com"), captor.capture());
        return captor.getValue();
    }

    private Order order(DeliveryMethod deliveryMethod, PaymentMethod paymentMethod) {
        var item = OrderItem.builder()
                .product(Product.builder().id(1L).name("Maki").build())
                .quantity(2)
                .subtotal(new BigDecimal("500.00"))
                .build();
        return Order.builder()
                .id(7L)
                .user(User.builder().email("anton@example.com").build())
                .customerName("Anton")
                .deliveryMethod(deliveryMethod)
                .paymentMethod(paymentMethod)
                .totalAmount(new BigDecimal("500.00"))
                .items(List.of(item))
                .build();
    }
}
