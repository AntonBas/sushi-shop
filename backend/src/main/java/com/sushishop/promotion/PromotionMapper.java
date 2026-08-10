package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.product.ProductEnrichmentService;
import com.sushishop.product.ProductMapper;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.promotion.dto.response.PromotionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class PromotionMapper {

    @Autowired
    protected ProductMapper productMapper;

    @Autowired
    protected ProductEnrichmentService productEnrichmentService;

    @Mapping(target = "products", source = "promotion", qualifiedByName = "mapProducts")
    public abstract PromotionResponse toResponse(Promotion promotion);

    @Named("mapProducts")
    protected List<ProductListResponse> mapProducts(Promotion promotion) {
        List<Long> productIds = promotion.getProducts().stream()
                .map(Product::getId)
                .toList();

        Map<Long, Double> ratings = productEnrichmentService.getAverageRatings(productIds);

        return promotion.getProducts().stream()
                .map(product -> productMapper.toListResponse(product, ratings.get(product.getId())))
                .toList();
    }
}