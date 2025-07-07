package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Kiểm tra xem tên lớp có đúng định dạng không
 * Định dạng được chấp nhận: 1A, 2B, 3C, 10A, 12D, v.v.
 */
@Documented
@Constraint(validatedBy = ClassNameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidClassName {
    String message() default "Tên lớp không hợp lệ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
