package com.sushishop.product;

import java.util.Comparator;

public class ProductImageMapper {

    public static String getMainImage(Product product) {
        if (product.getProductImages() == null || product.getProductImages().isEmpty()) return null;
        return product.getProductImages().stream()
                .min(Comparator.comparingInt(ProductImage::getSortOrder))
                .map(ProductImage::getUrl)
                .orElse(null);
    }
}