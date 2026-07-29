package com.lisovskyi.web.error.autoconfigure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class-level constraint that verifies two string fields on the annotated
 * object match each other. Commonly used on registration / password-change DTOs.
 *
 * <pre>{@code
 * @PasswordsMatch(
 *     originalPassword = "password",
 *     confirmPassword  = "confirmPassword",
 *     message          = "Passwords do not match"
 * )
 * public record RegisterRequest(String password, String confirmPassword) {}
 * }</pre>
 *
 * <p>The values of {@link #originalPassword()} and {@link #confirmPassword()} must
 * be the names of <em>no-arg getter methods</em> (or record accessors) on the
 * annotated type that return {@link String}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordsMatchValidator.class)
public @interface PasswordsMatch {

    /** Name of the getter / accessor that returns the original password. */
    String originalPassword() default "";

    /** Name of the getter / accessor that returns the confirmation password. */
    String confirmPassword() default "";

    String message() default "Passwords do not match";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
