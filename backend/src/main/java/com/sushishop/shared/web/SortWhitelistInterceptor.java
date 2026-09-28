package com.sushishop.shared.web;

import com.sushishop.shared.exception.core.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
public class SortWhitelistInterceptor implements HandlerInterceptor {

    private static final String SORT_PARAMETER = "sort";
    private static final Set<String> SORT_MODIFIERS = Set.of("asc", "desc", "ignorecase");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String[] sortValues = request.getParameterValues(SORT_PARAMETER);
        if (sortValues == null || !(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        findSortableParameter(handlerMethod).ifPresent(parameter -> validate(sortValues, allowedFields(parameter)));
        return true;
    }

    private Optional<MethodParameter> findSortableParameter(HandlerMethod handlerMethod) {
        return Arrays.stream(handlerMethod.getMethodParameters())
                .filter(parameter -> Pageable.class.isAssignableFrom(parameter.getParameterType())
                        || Sort.class.isAssignableFrom(parameter.getParameterType()))
                .findFirst();
    }

    private Set<String> allowedFields(MethodParameter parameter) {
        SortableFields sortableFields = parameter.getParameterAnnotation(SortableFields.class);
        return sortableFields == null ? Set.of() : Set.of(sortableFields.value());
    }

    private void validate(String[] sortValues, Set<String> allowedFields) {
        Arrays.stream(sortValues)
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::strip)
                .filter(token -> !token.isEmpty() && !SORT_MODIFIERS.contains(token.toLowerCase(Locale.ROOT)))
                .filter(token -> !allowedFields.contains(token))
                .findFirst()
                .ifPresent(token -> {
                    throw new BadRequestException(String.format("Unknown property '%s'", token));
                });
    }
}
