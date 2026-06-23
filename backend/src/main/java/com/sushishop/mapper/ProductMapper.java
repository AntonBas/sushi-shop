package com.sushishop.mapper;

import com.sushishop.domain.Product;
import com.sushishop.domain.ProductImage;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import org.mapstruct.*;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "category", expression = "java(product.getCategory().name())")
    @Mapping(target = "images", expression = "java(mapImages(product.getProductImages()))")
    ProductResponse toResponse(Product product);

    @Mapping(target = "category", expression = "java(product.getCategory().name())")
    @Mapping(target = "mainImage", expression = "java(getMainImage(product.getProductImages()))")
    ProductListResponse toListResponse(Product product);

    default List<String> mapImages(List<ProductImage> images) {
        if (images == null) return List.of();
        return images.stream()
                .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .toList();
    }

    default String getMainImage(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}