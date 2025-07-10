package com.fu.swp391.schoolhealthmanagementsystem.dto.admin;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidStaffRole;
import com.fu.swp391.schoolhealthmanagementsystem.validation.VietnamesePhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Yêu cầu tạo tài khoản nhân viên (Y tế, Quản lý Y tế)")
public record CreateStaffRequestDto(

        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 100)
        @Schema(
                description = "Họ tên đầy đủ của tài khoản nhân viên",
                example = "Trần Thị B"
        )
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100)
        @Schema(
                description = "Địa chỉ email",
                example = "staff@school.edu.vn"
        )
        String email,

        @VietnamesePhone
        @Schema(
                description = "Số điện thoại (tùy chọn)",
                example = "0987654321"
        )
        String phoneNumber,

        @NotNull(message = "Vai trò không được để trống")
        @ValidStaffRole
        @Schema(
                description = "Vai trò của nhân viên",
                example = "Nhân viên Y tế",
                allowableValues = {"Nhân viên Y tế", "Quản lý Nhân sự/Nhân viên"}
        )
        UserRole role
) {}
