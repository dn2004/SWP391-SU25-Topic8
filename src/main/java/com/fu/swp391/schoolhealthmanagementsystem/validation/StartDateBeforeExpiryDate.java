package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that scheduleStartDate is before expiryDate
 */
@Documented
@Constraint(validatedBy = StartDateBeforeExpiryDateValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface StartDateBeforeExpiryDate {
    String message() default "Ngày bắt đầu lịch trình phải trước ngày hết hạn thuốc";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
