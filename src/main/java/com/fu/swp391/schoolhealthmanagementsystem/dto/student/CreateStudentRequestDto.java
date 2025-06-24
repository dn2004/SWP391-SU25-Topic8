package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Yêu cầu tạo mới thông tin học sinh")
public record CreateStudentRequestDto(

        @NotBlank(message = "Mã học sinh không được để trống")
        @Size(max = 50, message = "Mã học sinh tối đa 50 ký tự")
        @Schema(description = "Mã học sinh (ví dụ: do trường cấp, phải là duy nhất)", example = "01")
        String studentCode,

        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
        @Schema(description = "Họ và tên đầy đủ của học sinh", example = "Nguyễn Thị C")
        String fullName,

        @NotNull(message = "Ngày sinh không được để trống")
        @PastOrPresent(message = "Ngày sinh phải là một ngày trong quá khứ hoặc hiện tại")
        @Schema(description = "Ngày sinh của học sinh", example = "2015-08-20")
        LocalDate dateOfBirth,

        @NotNull(message = "Giới tính không được để trống")
        @Schema(description = "Giá trị giới tính bao gồm: Nam/Nữ", example = "Nam")
        Gender gender,

        @NotBlank(message = "Tên lớp không được để trống")
        @Size(max = 50, message = "Tên lớp tối đa 50 ký tự")
        @Schema(description = "Lớp học của học sinh (ví dụ: 1A, 2B)", example = "3C")
        String className
) {}
