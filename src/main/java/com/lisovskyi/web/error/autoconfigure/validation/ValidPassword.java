package com.lisovskyi.web.error.autoconfigure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation constraint for verifying that a password string matches a specified regular expression.
 * <p>
 * This annotation allows specifying an array of messages instead of a single message.
 * When validation fails, all messages are added to the validation context as individual errors.
 * This is particularly useful for returning a list of password requirements to the frontend.
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

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
