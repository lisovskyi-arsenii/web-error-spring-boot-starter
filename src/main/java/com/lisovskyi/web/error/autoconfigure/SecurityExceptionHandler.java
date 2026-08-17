package com.lisovskyi.web.error.autoconfigure;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

/**
 * Handles Spring Security exceptions ({@link AccessDeniedException} — which
 * also covers the newer {@link org.springframework.security.authorization.AuthorizationDeniedException},
 * a subtype since Spring Security 6 — and {@link AuthenticationException}) and
 * maps them to structured {@link ErrorResponse} objects.
 * <p>
 * This class is only registered as a bean when Spring Security is present
 * on the classpath (see {@link WebErrorAutoConfiguration}).
 * <p>
 * {@code @Order(HIGHEST_PRECEDENCE)}: {@code ExceptionHandlerExceptionResolver}
 * picks the first {@code @ControllerAdvice} bean (by order, ties broken by
 * registration order) that declares <em>any</em> applicable {@code @ExceptionHandler}
 * method — it does not search all advice beans for the most specific match.
 * Without an explicit order this bean ties with {@link GlobalExceptionHandler},
 * whose catch-all {@code @ExceptionHandler(Exception.class)} would then win
 * for every security exception (registered first in {@link WebErrorAutoConfiguration}),
 * turning 401/403s into 500s. This must stay ordered before {@link GlobalExceptionHandler}.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityExceptionHandler {

    private final ErrorProperties properties;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied on path [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request, ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed on path [{}]: {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication required", request, ex);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
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
                .stackTrace(stackTrace)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
