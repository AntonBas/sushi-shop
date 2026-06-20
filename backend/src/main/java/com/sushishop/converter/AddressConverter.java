package com.sushishop.converter;

import com.sushishop.dto.response.AddressResponse;
import org.springframework.stereotype.Component;

@Component
public class AddressConverter {

    public AddressResponse toResponse(String city, String street, String house,
                                      String apartment, String comment) {
        if (city == null) return null;
        return new AddressResponse(city, street, house, apartment, comment);
    }
}