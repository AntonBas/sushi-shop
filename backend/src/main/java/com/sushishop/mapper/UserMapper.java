package com.sushishop.mapper;

import com.sushishop.domain.User;
import com.sushishop.dto.response.AddressResponse;
import com.sushishop.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "address", expression = "java(mapAddress(user))")
    UserResponse toResponse(User user);

    default AddressResponse mapAddress(User user) {
        if (user.getCity() == null && user.getStreet() == null && user.getHouse() == null) return null;
        return new AddressResponse(
                user.getCity(),
                user.getStreet(),
                user.getHouse(),
                user.getApartment(),
                null
        );
    }
}