package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class VietnamesePhoneValidator implements ConstraintValidator<VietnamesePhone, String> {
    private static final String VIETNAMESE_PHONE_REGEX = "^(0[35789])([0-9]{8})$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // Optional field, use @NotBlank for required
        }
        return value.matches(VIETNAMESE_PHONE_REGEX);
    }
}
