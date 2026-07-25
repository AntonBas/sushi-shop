package com.sushishop.mapper;

import com.sushishop.domain.Order;
import com.sushishop.domain.OrderItem;
import com.sushishop.dto.response.OrderItemResponse;
import com.sushishop.dto.response.OrderResponse;
import com.sushishop.dto.response.UserOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", imports = {ProductImageMapper.class})
public interface OrderMapper {

    @Mapping(target = "address.city", source = "city")
    @Mapping(target = "address.street", source = "street")
    @Mapping(target = "address.house", source = "house")
    @Mapping(target = "address.apartment", source = "apartment")
    @Mapping(target = "address.comment", source = "addressComment")
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(target = "items", source = "items")
    UserOrderResponse toUserResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "mainImage", expression = "java(ProductImageMapper.getMainImage(item.getProduct()))")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);
}