package com.sushishop.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Method;
import java.util.Objects;

public class FieldsMatchValidator implements ConstraintValidator<FieldsMatch, Object> {

    private String firstAccessor;
    private String secondAccessor;

    @Override
    public void initialize(FieldsMatch constraintAnnotation) {
        this.firstAccessor = constraintAnnotation.first();
        this.secondAccessor = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Method firstGetter = value.getClass().getMethod(firstAccessor);
            Method secondGetter = value.getClass().getMethod(secondAccessor);
            return Objects.equals(firstGetter.invoke(value), secondGetter.invoke(value));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Invalid @FieldsMatch accessor names: " + firstAccessor + ", " + secondAccessor, e);
        }
    }
}
