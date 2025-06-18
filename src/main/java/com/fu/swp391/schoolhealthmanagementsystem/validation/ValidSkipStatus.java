package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.validation.validator.SkipStatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SkipStatusValidator.class)
public @interface ValidSkipStatus {
    String message() default "Trạng thái lý do bỏ qua không hợp lệ. Chỉ chấp nhận: SKIPPED_STUDENT_ABSENT, SKIPPED_STUDENT_REFUSED, SKIPPED_SUPPLY_ISSUE, NOT_ADMINISTERED_OTHER.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
