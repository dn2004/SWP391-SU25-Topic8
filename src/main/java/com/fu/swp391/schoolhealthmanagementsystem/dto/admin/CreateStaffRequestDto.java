package com.fu.swp391.schoolhealthmanagementsystem.dto.admin;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidStaffRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu tạo tài khoản nhân viên (Y tế, Quản lý Y tế)")
public class CreateStaffRequestDto {
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100)
    @Schema(description = "Họ tên đầy đủ", example = "Trần Thị B")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100)
    @Schema(description = "Địa chỉ email", example = "staff@school.edu.vn")
    private String email;

    @Size(max = 20)
    @Schema(description = "Số điện thoại (tùy chọn)", example = "0987654321")
    private String phoneNumber;

    @NotNull(message = "Vai trò không được để trống")
    @ValidStaffRole // Custom validation annotation
    @Schema(description = "Vai trò của nhân viên", example = "MedicalStaff")
    private UserRole role;
}