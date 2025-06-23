package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Schema(description = "Xác định vai trò người dùng trong hệ thống.")
public enum UserRole {
    Parent("Phụ huynh"),
    MedicalStaff("Nhân viên Y tế"),
    StaffManager("Quản lý Nhân sự/Nhân viên"),
    SchoolAdmin("Quản trị viên Trường học");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static UserRole fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên hiển thị không được để trống.");
        }

        for (UserRole role : UserRole.values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
            if (role.name().equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy vai trò người dùng với tên: " + displayName);
    }
}