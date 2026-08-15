package com.sushishop.product;

import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductImageResponse;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductImageMapper.class})
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "available", ignore = true)
    Product toEntity(CreateProductRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "productImages", ignore = true)
    @Mapping(target = "promotions", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "available", ignore = true)
    void updateEntity(UpdateProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "images", expression = "java(mapImages(product.getProductImages()))")
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "discountPercent", ignore = true)
    @Mapping(target = "promotionTitle", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    ProductResponse toResponse(Product product);

    @Mapping(target = "mainImage", source = "product.productImages", qualifiedByName = "mainImage")
    @Mapping(target = "discountedPrice", source = "discountedPrice")
    @Mapping(target = "averageRating", source = "rating")
    ProductListResponse toListResponse(Product product, Double rating, BigDecimal discountedPrice);

    default List<ProductImageResponse> mapImages(List<ProductImage> images) {
        if (images == null) return List.of();
        return images.stream()
                .sorted(java.util.Comparator.comparingInt(ProductImage::getSortOrder))
                .map(img -> new ProductImageResponse(img.getId(), img.getUrl()))
                .toList();
    }
}