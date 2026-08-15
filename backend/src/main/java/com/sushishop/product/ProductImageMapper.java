package com.sushishop.product;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProductImageMapper {

    @Named("mainImage")
    public String getMainImage(List<ProductImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}