package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "Yêu cầu cập nhật thông tin học sinh")
public record UpdateStudentRequestDto(
        @Schema(description = "Họ và tên đầy đủ của học sinh")
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 100, message = "Họ và tên không được vượt quá 100 ký tự")
        String fullName,

        @Schema(description = "Ngày sinh của học sinh")
        @NotNull(message = "Ngày sinh không được để trống")
        @PastOrPresent(message = "Ngày sinh không hợp lệ")
        LocalDate dateOfBirth,

        @Schema(description = "Giới tính của học sinh")
        @NotNull(message = "Giới tính không được để trống")
        Gender gender,

        @Schema(description = "Tên lớp của học sinh")
        @NotBlank(message = "Tên lớp không được để trống")
        @Size(max = 50, message = "Tên lớp không được vượt quá 50 ký tự")
        String className
) {}
