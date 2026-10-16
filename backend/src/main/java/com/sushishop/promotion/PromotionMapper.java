package com.sushishop.promotion;

import com.sushishop.promotion.dto.PromotionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "products", ignore = true)
    @Mapping(target = "isCurrentlyActive", source = "currentlyActive")
    PromotionResponse toResponse(Promotion promotion);
}