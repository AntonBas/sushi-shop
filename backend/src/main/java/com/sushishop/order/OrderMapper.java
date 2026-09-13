package com.sushishop.order;

import com.sushishop.order.dto.response.OrderItemResponse;
import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import com.sushishop.product.ProductImageMapper;
import com.sushishop.shared.address.AddressResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductImageMapper.class})
public interface OrderMapper {

    @Mapping(target = "address", expression = "java(mapAddress(order))")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "paymentStatus", expression = "java(getPaymentStatus(order))")
    OrderResponse toResponse(Order order);

    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "paymentStatus", expression = "java(getPaymentStatus(order))")
    UserOrderResponse toUserResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "mainImage", source = "product.productImages", qualifiedByName = "mainImage")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);

    default AddressResponse mapAddress(Order order) {
        if (order.getCity() == null && order.getStreet() == null && order.getHouse() == null) return null;
        return new AddressResponse(
                order.getCity(),
                order.getStreet(),
                order.getHouse(),
                order.getApartment(),
                order.getAddressComment()
        );
    }

    default String getPaymentStatus(Order order) {
        if (order.getPaymentMethod() == PaymentMethod.ON_DELIVERY) return "ON_DELIVERY";
        return order.getStatus() == OrderStatus.NEW ? "PENDING" : "PAID";
    }
}