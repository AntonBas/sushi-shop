package com.sushishop.review;

import com.sushishop.review.dto.response.ReviewReplyResponse;
import com.sushishop.review.dto.response.ReviewResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "replies", source = "replies")
    ReviewResponse toResponse(Review review);

    @Mapping(target = "authorName", source = "user.name")
    ReviewReplyResponse toReplyResponse(ReviewReply reply);

    List<ReviewReplyResponse> toReplyResponseList(List<ReviewReply> replies);
}