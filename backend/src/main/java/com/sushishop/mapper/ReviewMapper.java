package com.sushishop.mapper;

import com.sushishop.domain.Review;
import com.sushishop.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface ReviewMapper {

    @Mapping(target = "userName", source = "user.name")
    ReviewResponse toResponse(Review review);
}
