package com.sushishop.shared.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares which entity properties a client may sort a {@code Pageable} parameter by.
 * A {@code sort} request parameter naming any other property is rejected with 400.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SortableFields {

    String[] value();
}
