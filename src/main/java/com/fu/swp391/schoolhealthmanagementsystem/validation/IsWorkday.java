package com.fu.swp391.schoolhealthmanagementsystem.validation; // Hoặc package của bạn

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = IsWorkdayValidator.class)
public @interface IsWorkday {
    String message() default "Ngày phải là ngày làm việc (Thứ 2 - Thứ 6)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}