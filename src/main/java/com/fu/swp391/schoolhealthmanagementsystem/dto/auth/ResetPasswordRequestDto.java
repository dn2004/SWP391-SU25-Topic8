package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu đặt lại mật khẩu bằng OTP")
public record ResetPasswordRequestDto(

        @NotBlank(message = "Email không được để trống")
        @Schema(description = "Email đã dùng để yêu cầu OTP", example = "user@example.com")
        String email,

        @NotBlank(message = "OTP không được để trống")
        @Schema(description = "Mã OTP nhận được qua email", example = "123456")
        String otp,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
        @Schema(description = "Mật khẩu mới", example = "newSecurePassword123")
        String newPassword

) {}
