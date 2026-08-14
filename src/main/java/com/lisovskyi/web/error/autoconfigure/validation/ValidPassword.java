package com.lisovskyi.web.error.autoconfigure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint for verifying password strength requirements.
 * <p>
 * This annotation allows specifying granular password policies, such as minimum and maximum
 * length, and requirements for uppercase letters, lowercase letters, digits, and special characters.
 * </p>
 * <p>
 * Instead of emitting a single constraint violation, this constraint will yield multiple violations
 * — one for each requirement that the password fails to meet. This is particularly useful for
 * returning a list of specific password requirements to the frontend.
 * </p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPasswordValidator.class)
public @interface ValidPassword {

    int minLength() default 8;
    int maxLength() default 72;

    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSpecialChar() default true;

    String message() default "Invalid password";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
