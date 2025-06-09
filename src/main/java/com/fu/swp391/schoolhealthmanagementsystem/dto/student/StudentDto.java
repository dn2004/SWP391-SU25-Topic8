package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thông tin chi tiết học sinh")
public record StudentDto(
        @Schema(description = "ID học sinh")
        Long studentId,

        @Schema(description = "Mã học sinh")
        String studentCode,

        @Schema(description = "Họ tên đầy đủ")
        String fullName,

        @Schema(description = "Ngày sinh")
        LocalDate dateOfBirth,

        @Schema(description = "Giới tính")
        Gender gender,

        @Schema(description = "Lớp học")
        String className,

        @Schema(description = "Địa chỉ")
        String address,

        @Schema(description = "Trạng thái hoạt động")
        boolean isActive,

        @Schema(description = "Mã mời cho phụ huynh")
        String invitationCode,

        @Schema(description = "Ngày tạo")
        LocalDateTime createdAt,

        @Schema(description = "Ngày cập nhật")
        LocalDateTime updatedAt
) {}
