package com.lisovskyi.web.error.autoconfigure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link PasswordsMatch} constraint.
 * Uses reflection to read two named accessor methods on the validated object
 * and compares their values for equality.
 */
public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, Object> {

    private String originalField;
    private String confirmField;

    @Override
    public void initialize(PasswordsMatch constraintAnnotation) {
        this.originalField = constraintAnnotation.originalPassword();
        this.confirmField  = constraintAnnotation.confirmPassword();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        try {
            Object originalValue = value.getClass().getMethod(originalField).invoke(value);
            Object confirmValue  = value.getClass().getMethod(confirmField).invoke(value);

            if (originalValue == null && confirmValue == null) {
                return true;
            }

            if (originalValue == null || confirmValue == null) {
                return false;
            }

            boolean matches = originalValue.equals(confirmValue);

            // Point the violation at the confirm-password field so the error appears
            // on the right field in the response (instead of at class level).
            if (!matches) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                        .addPropertyNode(confirmField)
                        .addConstraintViolation();
            }

            return matches;
        } catch (Exception e) {
            return false;
        }
    }
}
