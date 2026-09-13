package com.sushishop.promotion;

import com.sushishop.product.Product;
import com.sushishop.product.ProductEnrichmentService;
import com.sushishop.product.ProductMapper;
import com.sushishop.product.dto.response.ProductListResponse;
import com.sushishop.promotion.dto.response.PromotionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionResponseAssembler {

    private final PromotionMapper promotionMapper;
    private final ProductMapper productMapper;
    private final ProductEnrichmentService productEnrichmentService;

    public PromotionResponse toResponse(Promotion promotion) {
        return toResponseList(List.of(promotion)).get(0);
    }

    public List<PromotionResponse> toResponseList(List<Promotion> promotions) {
        if (promotions.isEmpty()) return List.of();

        List<Long> productIds = promotions.stream()
                .flatMap(p -> p.getProducts().stream())
                .map(Product::getId)
                .distinct()
                .toList();

        Map<Long, Double> ratings = productEnrichmentService.getAverageRatings(productIds);

        return promotions.stream().map(promotion -> assemble(promotion, ratings)).toList();
    }

    private PromotionResponse assemble(Promotion promotion, Map<Long, Double> ratings) {
        BigDecimal discount = promotion.isCurrentlyActive() ? promotion.getDiscountPercent().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP) : null;

        List<ProductListResponse> productResponses = promotion.getProducts().stream().map(product -> {
            BigDecimal discountedPrice = discount != null ? product.getPrice().multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP) : null;
            return productMapper.toListResponse(product, ratings.get(product.getId()), discountedPrice);
        }).toList();

        var response = promotionMapper.toResponse(promotion);
        return new PromotionResponse(response.id(), response.slug(), response.title(), response.description(), response.discountPercent(), response.startDate(), response.endDate(), response.active(), promotion.isCurrentlyActive(), productResponses);
    }
}