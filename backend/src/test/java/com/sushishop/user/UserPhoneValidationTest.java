package com.sushishop.user;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPhoneValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptPhoneWithPlusAndFifteenDigitsAllowedByRequestPattern() {
        var user = User.builder().phone("+380961791111222").build();

        assertThat(validator.validateProperty(user, "phone")).isEmpty();
    }
}
