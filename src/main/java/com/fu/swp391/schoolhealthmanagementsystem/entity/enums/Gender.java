package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Schema(description = "Giới tính của cá nhân")
@Slf4j
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Gender fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        String trimmedDisplayName = displayName.trim();
        for (Gender gender : Gender.values()) {
            if (gender.getDisplayName().equalsIgnoreCase(trimmedDisplayName) || gender.name().equalsIgnoreCase(trimmedDisplayName)) {
                return gender;
            }
        }
        // If no match is found, throw a clear exception.
        throw new IllegalArgumentException("Không tìm thấy giới tính phù hợp với giá trị: '" + displayName + "'. Các giá trị hợp lệ là 'Nam', 'Nữ', 'MALE', 'FEMALE'.");
    }
}