package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Yêu cầu đăng nhập")
public record LoginRequestDto(

        @Schema(
                description = "Email của người dùng",
                example = "user@example.com"
        )
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        String email,

        @Schema(
                description = "Mật khẩu của người dùng",
                example = "password123"
        )
        @NotBlank(message = "Mật khẩu không được để trống")
        String password

) {}
