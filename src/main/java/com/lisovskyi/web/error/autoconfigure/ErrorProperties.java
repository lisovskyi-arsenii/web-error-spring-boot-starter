package com.lisovskyi.web.error.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.web.error")
public class ErrorProperties {
    /**
     * Whether the global exception handler is enabled.
     */
    private boolean enabled = true;

    /**
     * Whether to include stack traces in error responses.
     * <strong>Keep false in production</strong> — stack traces may expose
     * internal implementation details to clients.
     */
    private boolean includeStackTrace = false;

    /**
     * Whether to include field-level validation errors for {@code @Valid} objects.
     */
    private boolean includeFieldErrors = true;

    /**
     * Whether to include the rejected (user-supplied) value in field validation errors.
     * Disable this if any validated fields may carry sensitive data (passwords, tokens, etc.).
     */
    private boolean includeRejectedValues = false;
}
