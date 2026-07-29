package com.lisovskyi.web.error.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ErrorProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "app.web.error", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebErrorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ErrorProperties properties) {
        return new GlobalExceptionHandler(properties);
    }

    /**
     * Registers a dedicated handler for Spring Security exceptions
     * (AccessDeniedException → 403, AuthenticationException → 401).
     * Only activated when Spring Security is present on the classpath,
     * preventing a hard dependency for non-security applications.
     */
    @Bean
    @ConditionalOnMissingBean(SecurityExceptionHandler.class)
    @ConditionalOnClass(name = "org.springframework.security.access.AccessDeniedException")
    public SecurityExceptionHandler securityExceptionHandler(ErrorProperties properties) {
        return new SecurityExceptionHandler(properties);
    }
}
