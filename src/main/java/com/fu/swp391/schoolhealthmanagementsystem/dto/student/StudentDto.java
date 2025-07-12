package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;


import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thông tin chi tiết học sinh")
public record StudentDto(
        @Schema(
                description = "ID học sinh",
                example = "101"
        )
        Long id,

        @Schema(
                description = "Họ tên đầy đủ",
                example = "Nguyễn Văn A"
        )
        String fullName,

        @Schema(
                description = "Ngày sinh",
                example = "2015-09-01"
        )
        LocalDate dateOfBirth,

        @Schema(
                description = "Giới tính",
                example = "Nam"
        )
        Gender gender,

        @Schema(
                description = "Lớp học của học sinh",
                example = "Mầm A"
        )
        String className,

        @Schema(
                description = "Khối lớp (Mầm, Chồi, Lá)",
                example = "Mầm"
        )
        String classGroup,

        @Schema(
                description = "Tên hiển thị của trạng thái",
                example = "Đang học"
        )
        StudentStatus status,

        @Schema(
                description = "Mã mời cho phụ huynh",
                example = "INVITE12345"
        )
        String invitationCode,

        @Schema(
                description = "Ngày tạo",
                example = "2024-08-01T08:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Ngày cập nhật",
                example = "2025-07-09T10:00:00"
        )
        LocalDateTime updatedAt
) {}
