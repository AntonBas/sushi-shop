package com.sushishop.user;

import com.sushishop.shared.address.AddressResponse;
import com.sushishop.user.dto.response.UserResponse;
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