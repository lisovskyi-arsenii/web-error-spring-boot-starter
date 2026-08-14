package com.lisovskyi.web.error.autoconfigure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for the {@link ValidPassword} constraint.
 * <p>
 * If the value is {@code null}, the validator considers it valid. Use {@code @NotNull} or
 * {@code @NotBlank} if the password is required.
 * </p>
 * <p>
 * When the password fails to match the regular expression, this validator loops through the
 * array of messages defined in {@link ValidPassword#messages()} and emits a constraint violation
 * for each message. This ensures the client receives a detailed list of requirements.
 * </p>
 */
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private Pattern pattern;
    private String[] messages;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.pattern = Pattern.compile(constraintAnnotation.regex());
        this.messages = constraintAnnotation.messages();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (pattern.matcher(value).matches()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        for (String msg : messages) {
            context.buildConstraintViolationWithTemplate(msg)
                    .addConstraintViolation();
        }

        return false;
    }
}
