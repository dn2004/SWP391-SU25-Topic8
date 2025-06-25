package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thông tin chi tiết học sinh")
public record StudentDto(
        @Schema(description = "ID học sinh")
        Long id,

        @Schema(description = "Họ tên đầy đủ")
        String fullName,

        @Schema(description = "Ngày sinh")
        LocalDate dateOfBirth,

        @Schema(description = "Giới tính")
        String gender,

        @Schema(description = "Lớp học")
        String className,

        @Schema(description = "Tên hiển thị của trạng thái")
        String status,

        @Schema(description = "Mã mời cho phụ huynh")
        String invitationCode,

        @Schema(description = "Ngày tạo")
        LocalDateTime createdAt,

        @Schema(description = "Ngày cập nhật")
        LocalDateTime updatedAt
) {}
