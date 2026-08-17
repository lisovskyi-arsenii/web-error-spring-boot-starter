package com.lisovskyi.web.error.autoconfigure;

import com.lisovskyi.web.error.autoconfigure.base.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

/**
 * Holds the catch-all {@code @ExceptionHandler(Exception.class)} fallback, so
 * it must be ordered <em>after</em> more specific advice beans such as
 * {@link SecurityExceptionHandler} — otherwise this bean's catch-all would
 * shadow their more specific handlers (see {@link SecurityExceptionHandler}
 * for why).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
    private final ErrorProperties properties;

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        log.warn("Application exception [{}]: {}", ex.getCode(), ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), request, null, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation failed for request path: {}", request.getRequestURI());

        List<FieldErrorDto> fieldErrors = null;
        if (properties.isIncludeFieldErrors()) {
            fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> FieldErrorDto.builder()
                            .field(error.getField())
                            // rejectedValue is opt-in: disable if validated fields may carry
                            // sensitive data (passwords, tokens, card numbers, etc.)
                            .rejectedValue(properties.isIncludeRejectedValues() ? error.getRejectedValue() : null)
                            .message(error.getDefaultMessage())
                            .build())
                    .toList();
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Validation failed for one or more fields", request, fieldErrors, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "MALFORMED_JSON", "Malformed JSON request body", request, null, ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("HTTP method not supported: {}", ex.getMethod());
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", String.format("HTTP method '%s' is not supported for this endpoint", ex.getMethod()), request, null, ex);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Method argument type mismatch: parameter '{}'", ex.getName());
        String message = String.format("Parameter '%s' should be of type '%s'", ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildResponse(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, request, null, ex);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument on path [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request, null, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on path [{}]: ", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "An unexpected internal server error occurred", request, null, ex);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            List<FieldErrorDto> fieldErrors,
            Exception ex
    ) {
        String stackTrace = null;
        if (properties.isIncludeStackTrace()) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString();
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .stackTrace(stackTrace)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
