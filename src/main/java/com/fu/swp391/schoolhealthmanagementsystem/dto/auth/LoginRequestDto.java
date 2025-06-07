package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Yêu cầu đăng nhập")
public record LoginRequestDto(

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Schema(example = "user@example.com")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Schema(example = "password123")
        String password

) {}
