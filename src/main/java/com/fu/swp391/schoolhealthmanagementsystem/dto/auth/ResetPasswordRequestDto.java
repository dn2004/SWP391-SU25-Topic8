package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu đặt lại mật khẩu bằng OTP")
@PasswordMatches(passwordField = "newPassword", confirmPasswordField = "confirmNewPassword")
public record ResetPasswordRequestDto(

        @Schema(
                description = "Email đã dùng để yêu cầu OTP",
                example = "user@example.com"
        )
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @Schema(
                description = "Mật khẩu mới",
                example = "newPassword123"
        )
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(
                min = 6,
                message = "Mật khẩu phải có ít nhất 6 ký tự"
        )
        String newPassword,

        @Schema(
                description = "Mã OTP",
                example = "123456"
        )
        @NotBlank(message = "OTP không được để trống")
        String otp
) {}
