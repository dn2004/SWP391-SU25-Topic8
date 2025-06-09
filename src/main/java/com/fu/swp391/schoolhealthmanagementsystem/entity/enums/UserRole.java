package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Schema(description = "Xác định vai trò người dùng trong hệ thống.",
        allowableValues = {"Parent", "MedicalStaff", "StaffManager", "SchoolAdmin"})
public enum UserRole {
    Parent("Phụ huynh"),
    MedicalStaff("Nhân viên Y tế"),
    StaffManager("Quản lý Nhân sự/Nhân viên"),
    SchoolAdmin("Quản trị viên Trường học");

    private final String vietnameseName;

    UserRole(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public static UserRole fromVietnameseName(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Tên vai trò tiếng Việt đầu vào cho UserRole là null hoặc rỗng.");
            throw new IllegalArgumentException("Tên vai trò tiếng Việt không được để trống.");
        }

        for (UserRole role : UserRole.values()) {
            if (role.vietnameseName.equalsIgnoreCase(text)) {
                return role;
            }
            if (role.name().equalsIgnoreCase(text)) {
                log.warn("Tìm thấy UserRole bằng tên hằng số '{}' thay vì vietnameseName. Đầu vào là: '{}'", role.name(), text);
                return role;
            }
        }
        log.warn("Không tìm thấy UserRole cho tên tiếng Việt: '{}'. Ném IllegalArgumentException.", text);
        throw new IllegalArgumentException("Không tìm thấy vai trò người dùng với tên tiếng Việt: " + text);
    }
}