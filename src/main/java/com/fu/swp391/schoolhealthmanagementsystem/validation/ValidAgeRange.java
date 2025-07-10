package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = ValidAgeRangeValidator.class)
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ValidAgeRange {
    String message() default "Tuổi tối thiểu phải nhỏ hơn hoặc bằng tuổi tối đa";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
