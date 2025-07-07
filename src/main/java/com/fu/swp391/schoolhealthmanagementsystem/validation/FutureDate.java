package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Annotation để kiểm tra một ngày phải trong tương lai (lớn hơn ngày hiện tại)
 * và cách ngày hiện tại một khoảng thời gian tối thiểu
 */
@Documented
@Constraint(validatedBy = FutureDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FutureDate {
    String message() default "Ngày phải lớn hơn ngày hiện tại";

    /**
     * Số ngày tối thiểu kể từ ngày hiện tại
     */
    int days() default 0;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
