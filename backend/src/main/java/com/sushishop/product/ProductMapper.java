package com.sushishop.product;

import com.sushishop.product.dto.request.CreateProductRequest;
import com.sushishop.product.dto.request.UpdateProductRequest;
import com.sushishop.product.dto.response.ProductImageResponse;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.product.dto.response.ProductResponse;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductEnrichmentService.class})
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
    void updateEntity(UpdateProductRequest request, @MappingTarget Product entity);

    @Mapping(target = "images", expression = "java(mapImages(product))")
    @Mapping(target = "discountedPrice", source = "product", qualifiedByName = "discountedPrice")
    @Mapping(target = "discountPercent", source = "product", qualifiedByName = "discountPercent")
    @Mapping(target = "promotionTitle", source = "product", qualifiedByName = "promotionTitle")
    @Mapping(target = "reviewCount", expression = "java(product.getReviews().size())")
    @Mapping(target = "averageRating", expression = "java(product.getReviews().isEmpty() ? null : product.getReviews().stream().mapToInt(r -> r.getRating()).average().orElse(0.0))")
    ProductResponse toResponse(Product product);

    @Mapping(target = "mainImage", expression = "java(ProductImageMapper.getMainImage(product))")
    @Mapping(target = "discountedPrice", source = "discountedPrice")
    @Mapping(target = "averageRating", source = "rating")
    ProductListResponse toListResponse(Product product, Double rating, BigDecimal discountedPrice);

    default List<ProductImageResponse> mapImages(Product product) {
        return product.getProductImages().stream()
                .sorted(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(img -> new ProductImageResponse(img.getId(), img.getUrl()))
                .toList();
    }
}