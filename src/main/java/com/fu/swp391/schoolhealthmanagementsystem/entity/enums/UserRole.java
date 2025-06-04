package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "Defines the roles a user can have within the system.",
        allowableValues = {"Parent", "MedicalStaff", "SchoolAdmin"})
public enum UserRole {
    Parent,
    MedicalStaff,
    StaffManager,
    SchoolAdmin
}