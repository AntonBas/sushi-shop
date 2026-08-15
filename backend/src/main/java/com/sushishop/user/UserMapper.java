package com.sushishop.user;

import com.sushishop.auth.dto.request.RegisterRequest;
import com.sushishop.shared.address.AddressConverter;
import com.sushishop.user.dto.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected AddressConverter addressConverter;

    @Mapping(target = "address", expression = "java(addressConverter.toResponse(user.getCity(), user.getStreet(), user.getHouse(), user.getApartment(), null))")
    public abstract UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "tokenVersion", constant = "0")
    @Mapping(target = "userRole", constant = "CUSTOMER")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "house", source = "address.house")
    @Mapping(target = "apartment", source = "address.apartment")
    public abstract User toEntity(RegisterRequest request);
}