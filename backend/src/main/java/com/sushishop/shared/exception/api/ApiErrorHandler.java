package com.sushishop.shared.exception.api;

import com.sushishop.shared.exception.core.RateLimitExceededException;
import com.sushishop.shared.exception.core.SushiShopException;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CONTENT_TOO_LARGE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class ApiErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(@Nonnull MaxUploadSizeExceededException ex,
                                                                          @Nonnull HttpHeaders headers,
                                                                          @Nonnull HttpStatusCode status,
                                                                          @Nonnull WebRequest request) {
        log.warn("Upload rejected: {}", ex.getMessage());
        return buildResponseEntity(new ApiError(CONTENT_TOO_LARGE,
                "Uploaded files are too large: max 5 MB per image and 30 MB per request"), request);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@Nonnull HttpMessageNotReadableException ex,
                                                                  @Nonnull HttpHeaders headers,
                                                                  @Nonnull HttpStatusCode status,
                                                                  @Nonnull WebRequest request) {
        String error = "Malformed JSON request";
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildResponseEntity(new ApiError(BAD_REQUEST, error), request);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@Nonnull MethodArgumentNotValidException ex,
                                                                  @Nonnull HttpHeaders headers,
                                                                  @Nonnull HttpStatusCode status,
                                                                  @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
        log.warn("Validation error: {} errors detected", ex.getBindingResult().getFieldErrors().size());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(@Nonnull EntityNotFoundException ex,
                                                          @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        log.warn("Entity not found: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    protected ResponseEntity<Object> handleRateLimit(@Nonnull RateLimitExceededException ex,
                                                     @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(TOO_MANY_REQUESTS);
        apiError.setMessage("Rate limit exceeded. Please try again later.");
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        if (ex.getRetryAfterSeconds() != null) {
            headers.add(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
        }
        return buildResponseEntity(apiError, request, headers);
    }

    @ExceptionHandler(SushiShopException.class)
    protected ResponseEntity<Object> handleSushiShopException(@Nonnull SushiShopException ex,
                                                              @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(ex.getStatus());
        apiError.setMessage(ex.getMessage());
        apiError.setCode(ex.getCode());
        if (ex.getStatus().is5xxServerError()) {
            log.error("Server exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        } else {
            log.warn("Business exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());
        }
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(@Nonnull DataIntegrityViolationException ex,
                                                                  @Nonnull WebRequest request) {
        if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            ApiError apiError = new ApiError(CONFLICT, "Database constraint violation");
            log.warn("Database constraint violation: {}", ex.getMessage());
            return buildResponseEntity(apiError, request);
        }
        ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR, "Database error");
        log.error("Database error: ", ex);
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    protected ResponseEntity<Object> handleOptimisticLocking(@Nonnull OptimisticLockingFailureException ex,
                                                             @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(CONFLICT, "The resource was modified concurrently, please retry");
        log.warn("Optimistic locking failure: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(TransactionSystemException.class)
    protected ResponseEntity<Object> handleTransactionSystem(@Nonnull TransactionSystemException ex,
                                                             @Nonnull WebRequest request) {
        if (ex.getRootCause() instanceof ConstraintViolationException constraintViolation) {
            return handleConstraintViolation(constraintViolation, request);
        }
        return handleAllExceptions(ex, request);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    protected ResponseEntity<Object> handlePropertyReference(@Nonnull PropertyReferenceException ex,
                                                             @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST, String.format("Unknown property '%s'", ex.getPropertyName()));
        log.warn("Invalid property reference: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(@Nonnull MethodArgumentTypeMismatchException ex,
                                                                      @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        String requiredType = Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("unknown");
        apiError.setMessage(String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), requiredType));
        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(@Nonnull ConstraintViolationException ex,
                                                               @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error");
        ex.getConstraintViolations().forEach(cv -> apiError.addValidationError(cv.getRootBeanClass().getSimpleName(),
                cv.getPropertyPath().toString(), cv.getInvalidValue(), cv.getMessage()));
        log.warn("Constraint violation: {} violations", ex.getConstraintViolations().size());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentials(@Nonnull BadCredentialsException ex,
                                                          @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(UNAUTHORIZED, "Invalid email or password");
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(@Nonnull AccessDeniedException ex,
                                                        @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(FORBIDDEN, "Access denied");
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(@Nonnull Exception ex, @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR, "Unexpected error occurred");
        log.error("Unexpected error: ", ex);
        return buildResponseEntity(apiError, request);
    }

    @Nonnull
    private ResponseEntity<Object> buildResponseEntity(@Nonnull ApiError apiError, @Nonnull WebRequest request) {
        return buildResponseEntity(apiError, request, new HttpHeaders());
    }

    @Nonnull
    private ResponseEntity<Object> buildResponseEntity(@Nonnull ApiError apiError, @Nonnull WebRequest request,
                                                        @Nonnull HttpHeaders headers) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            apiError.setPath(servletWebRequest.getRequest().getRequestURI());
        } else {
            apiError.setPath("unknown");
        }
        return new ResponseEntity<>(apiError, headers,
                Objects.requireNonNull(apiError.getStatus(), "ApiError status must not be null"));
    }
}