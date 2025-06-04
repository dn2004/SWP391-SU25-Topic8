package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu gửi OTP để đặt lại mật khẩu")
public class ForgotPasswordRequestDto {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email đã đăng ký để nhận OTP", example = "user@example.com")
    private String email;
}