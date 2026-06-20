package com.sushishop.mapper;

import com.sushishop.domain.Product;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "category", expression = "java(product.getCategory().name())")
    ProductResponse toResponse(Product product);

    @Mapping(target = "category", expression = "java(product.getCategory().name())")
    ProductListResponse toListResponse(Product product);
}
