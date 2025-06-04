package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE}) // Áp dụng cho class
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
public @interface PasswordMatches {
    String message() default "Mật khẩu và xác nhận mật khẩu không khớp!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    // Các trường cần so sánh (mặc định là "password" và "confirmPassword")
    String passwordField() default "password";
    String confirmPasswordField() default "confirmPassword";
}