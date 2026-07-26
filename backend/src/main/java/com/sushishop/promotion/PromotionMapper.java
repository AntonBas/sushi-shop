package com.sushishop.promotion;

import com.sushishop.promotion.dto.response.PromotionResponse;
import com.sushishop.product.ProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface PromotionMapper {

    @Mapping(target = "products", source = "products")
    PromotionResponse toResponse(Promotion promotion);
}
