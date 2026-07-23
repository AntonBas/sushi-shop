package com.sushishop.mapper;

import com.sushishop.domain.Product;
import com.sushishop.domain.ProductImage;
import com.sushishop.dto.request.CreateProductRequest;
import com.sushishop.dto.request.UpdateProductRequest;
import com.sushishop.dto.response.ProductListResponse;
import com.sushishop.dto.response.ProductResponse;
import org.mapstruct.*;

import java.util.Comparator;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "available", ignore = true)
    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "available", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "promotionTitle", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    ProductResponse toResponse(Product product);

    @Mapping(target = "mainImage", expression = "java(getMainImage(product))")
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    ProductListResponse toListResponse(Product product);

    default String getMainImage(Product product) {
        if (product.getProductImages() == null || product.getProductImages().isEmpty()) return null;
        return product.getProductImages().stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}