package com.sushishop.mapper;

import com.sushishop.domain.Order;
import com.sushishop.domain.OrderItem;
import com.sushishop.dto.response.OrderItemResponse;
import com.sushishop.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "mainImage", expression = "java(ProductImageMapper.getMainImage(item.getProduct()))")
    OrderItemResponse toItemResponse(OrderItem item);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> items);
}