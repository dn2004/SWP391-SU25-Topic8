package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Yêu cầu OTP")
public record OtpRequestDto(
        @Schema(
                description = "Email người dùng để nhận mã OTP",
                example = "user@example.com"
        )
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @Schema(
                description = "Mã OTP",
                example = "123456"
        )
        @NotBlank(message = "OTP không được để trống")
        String otp
) {}
