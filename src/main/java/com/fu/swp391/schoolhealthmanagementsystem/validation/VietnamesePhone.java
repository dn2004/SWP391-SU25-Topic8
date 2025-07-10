package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = VietnamesePhoneValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface VietnamesePhone {
    String message() default "Số điện thoại không đúng định dạng Việt Nam";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
