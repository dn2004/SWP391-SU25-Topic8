package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

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
        @Schema(description = "Mã học sinh (ví dụ: do trường cấp, phải là duy nhất)", example = "HS00123")
        String studentCode,

        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
        @Schema(description = "Họ và tên đầy đủ của học sinh", example = "Nguyễn Thị C")
        String fullName,

        @NotNull(message = "Ngày sinh không được để trống")
        @PastOrPresent(message = "Ngày sinh phải là một ngày trong quá khứ hoặc hiện tại")
        @Schema(description = "Ngày sinh của học sinh", example = "2015-08-20")
        LocalDate dateOfBirth,

        @NotBlank(message = "Giới tính không được để trống")
        @Size(max = 10, message = "Giới tính tối đa 10 ký tự")
        @Schema(description = "Giới tính (ví dụ: Nam, Nữ, Khác)", example = "Nữ")
        String gender,

        @NotBlank(message = "Tên lớp không được để trống")
        @Size(max = 50, message = "Tên lớp tối đa 50 ký tự")
        @Schema(description = "Lớp học của học sinh (ví dụ: 1A, 2B)", example = "3C")
        String className,

        @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
        @Schema(description = "Địa chỉ của học sinh (tùy chọn)", example = "123 Đường ABC, Phường XYZ, Quận UVW, Thành phố H")
        String address

) {}
