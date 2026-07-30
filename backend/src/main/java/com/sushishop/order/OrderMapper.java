package com.sushishop.order;

import com.sushishop.product.ProductImageMapper;
import com.sushishop.shared.address.AddressResponse;
import com.sushishop.order.dto.response.OrderItemResponse;
import com.sushishop.order.dto.response.OrderResponse;
import com.sushishop.order.dto.response.UserOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", imports = {ProductImageMapper.class})
public interface OrderMapper {

    @Mapping(target = "address", expression = "java(mapAddress(order))")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(target = "items", source = "items")
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