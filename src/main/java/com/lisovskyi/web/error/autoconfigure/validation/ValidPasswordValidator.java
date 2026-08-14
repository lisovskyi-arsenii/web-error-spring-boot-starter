package com.lisovskyi.web.error.autoconfigure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the {@link ValidPassword} constraint.
 * <p>
 * If the value is {@code null}, the validator considers it valid. Use {@code @NotNull} or
 * {@code @NotBlank} if the password is required.
 * </p>
 * <p>
 * When the password fails to meet the specified policies, this validator disables the default
 * constraint violation and instead emits a distinct violation for each rule (length, uppercase,
 * lowercase, digit, special character) that was not satisfied. This ensures the client receives
 * a detailed and granular list of requirements.
 * </p>
 */
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSpecialChar;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();

        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecialChar = constraintAnnotation.requireSpecialChar();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (value.length() < minLength || value.length() > maxLength) {
            context.buildConstraintViolationWithTemplate(
                            "Password must be between " + minLength + " and " + maxLength + " characters")
                    .addConstraintViolation();
            valid = false;
        }

        if (requireUppercase && value.chars().noneMatch(Character::isUpperCase)) {
            context.buildConstraintViolationWithTemplate("Password must contain an uppercase letter")
                    .addConstraintViolation();
            valid = false;
        }

        if (requireLowercase && value.chars().noneMatch(Character::isLowerCase)) {
            context.buildConstraintViolationWithTemplate("Password must contain a lowercase letter")
                    .addConstraintViolation();
            valid = false;
        }

        if (requireDigit && value.chars().noneMatch(Character::isDigit)) {
            context.buildConstraintViolationWithTemplate("Password must contain a digit")
                    .addConstraintViolation();
            valid = false;
        }

        if (requireSpecialChar && value.chars().allMatch(Character::isLetterOrDigit)) {
            context.buildConstraintViolationWithTemplate("Password must contain a special character")
                    .addConstraintViolation();
            valid = false;
        }

        return valid;
    }
}
