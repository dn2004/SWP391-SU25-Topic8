package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * Enum representing the possible status values for a Student entity.
 */
@Getter
@Schema(description = "Trạng thái của học sinh")
public enum StudentStatus {
    ACTIVE("Hoạt Động"),     // Student is currently active and enrolled
    GRADUATED("Tốt Nghiệp"),  // Student has graduated
    WITHDRAWN("Thôi Học");  // Student has been withdrawn from school

    private final String displayName;

    StudentStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static StudentStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        String trimmedDisplayName = displayName.trim();
        for (StudentStatus status : StudentStatus.values()) {
            if (status.displayName.equalsIgnoreCase(trimmedDisplayName) || status.name().equalsIgnoreCase(trimmedDisplayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy StudentStatus với displayName: " + displayName);
    }
}
