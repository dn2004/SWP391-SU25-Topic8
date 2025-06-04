package com.fu.swp391.schoolhealthmanagementsystem.dto.user;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin chi tiết người dùng")
public record UserDto(
        @Schema(description = "ID người dùng", example = "1")
        Long userId,

        @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
        String fullName,

        @Schema(description = "Địa chỉ email", example = "user@example.com")
        String email,

        @Schema(description = "Số điện thoại", example = "0901234567")
        String phoneNumber,

        @Schema(description = "Vai trò người dùng")
        UserRole role,

        @Schema(description = "Trạng thái hoạt động", example = "true")
        boolean isActive,

        @Schema(description = "UID từ Firebase (nếu có)", example = "firebaseUid123")
        String firebaseUid,

        @Schema(description = "Phụ huynh đã liên kết với học sinh chưa?", example = "false")
        boolean linkedToStudent
) {}
