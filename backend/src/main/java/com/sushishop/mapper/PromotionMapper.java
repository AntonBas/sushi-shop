package com.sushishop.mapper;

import com.sushishop.domain.Promotion;
import com.sushishop.dto.response.PromotionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface PromotionMapper {

    @Mapping(target = "products", source = "products")
    PromotionResponse toResponse(Promotion promotion);
}
