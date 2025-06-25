package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the possible status values for a Student entity.
 */
public enum StudentStatus {
    ACTIVE("Hoạt Động"),     // Student is currently active and enrolled
    GRADUATED("Tốt Nghiệp"),  // Student has graduated
    TRANSFERRED("Chuyển Trường"), // Student has transferred to another school
    WITHDRAWN("Thôi Học");  // Student has been withdrawn from school

    private final String displayName;

    StudentStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }


    public static StudentStatus fromString(String name) {
        if (name == null) {
            return null;
        }

        for (StudentStatus status : StudentStatus.values()) {
            if (status.name().equalsIgnoreCase(name) || status.getDisplayName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }
}
