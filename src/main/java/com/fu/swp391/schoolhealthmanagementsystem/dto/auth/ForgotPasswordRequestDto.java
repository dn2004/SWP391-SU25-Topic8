package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Yêu cầu quên mật khẩu")
public record ForgotPasswordRequestDto(

        @Schema(
                description = "Email đã đăng ký để nhận OTP",
                example = "user@example.com"
        )
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email
) {}
