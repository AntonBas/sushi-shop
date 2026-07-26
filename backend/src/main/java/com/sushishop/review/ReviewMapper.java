package com.sushishop.review;

import com.sushishop.review.dto.response.ReviewResponse;
import com.sushishop.product.ProductMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface ReviewMapper {

    @Mapping(target = "userName", source = "user.name")
    ReviewResponse toResponse(Review review);
}
