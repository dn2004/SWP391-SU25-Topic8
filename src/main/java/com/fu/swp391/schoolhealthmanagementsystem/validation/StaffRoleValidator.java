package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StaffRoleValidator implements ConstraintValidator<ValidStaffRole, UserRole> {
    @Override
    public boolean isValid(UserRole value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return value == UserRole.MedicalStaff || value == UserRole.StaffManager;
    }
}