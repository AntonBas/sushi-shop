package com.sushishop.mapper;

import com.sushishop.domain.User;
import com.sushishop.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "address", ignore = true)
    UserResponse toResponse(User user);
}