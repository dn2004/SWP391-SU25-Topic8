package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Schema(description = "Giới tính của cá nhân",
        allowableValues = {"MALE", "FEMALE"})
@Slf4j
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public static Gender fromDisplayName(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Giá trị displayName đầu vào cho Gender là null hoặc rỗng. Trả về null.");
            return null;
        }
        for (Gender gender : Gender.values()) {
            if (gender.displayName.equalsIgnoreCase(text)) {
                return gender;
            }
            if (gender.name().equalsIgnoreCase(text)) {
                return gender;
            }
        }
        log.warn("Không tìm thấy Gender cho giá trị displayName: '{}'. Ném IllegalArgumentException.", text);
        throw new IllegalArgumentException("Không tìm thấy giới tính với tên hiển thị: " + text);
    }
}