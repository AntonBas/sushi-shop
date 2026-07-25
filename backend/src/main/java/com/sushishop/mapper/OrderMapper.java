package com.sushishop.mapper;

import com.sushishop.domain.Order;
import com.sushishop.domain.OrderItem;
import com.sushishop.dto.response.AddressResponse;
import com.sushishop.dto.response.OrderItemResponse;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.dto.response.UserOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", imports = {ProductImageMapper.class})
public interface OrderMapper {

    @Mapping(target = "address", expression = "java(mapAddress(order))")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(target = "items", source = "items")
    @Mapping(target = "deliveryMethod", source = "deliveryMethod")
    UserOrderResponse toUserResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "mainImage", expression = "java(ProductImageMapper.getMainImage(item.getProduct()))")
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
}