package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StaffRoleValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStaffRole {
    String message() default "Vai trò nhân viên không hợp lệ. Chỉ chấp nhận MedicalStaff hoặc StaffManager.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}