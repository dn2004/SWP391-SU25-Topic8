package com.fu.swp391.schoolhealthmanagementsystem.dto.user;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu đổi mật khẩu hiện tại")
@PasswordMatches(passwordField = "newPassword", confirmPasswordField = "confirmNewPassword")
public record ChangePasswordRequestDto(

        @NotBlank(message = "Mật khẩu cũ không được để trống")
        @Schema(
                description = "Mật khẩu hiện tại của người dùng",
                example = "currentPassword123"
        )
        String oldPassword,

        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(
                min = 6,
                max = 255,
                message = "Mật khẩu mới phải từ 6 đến 255 ký tự"
        )
        @Schema(
                description = "Mật khẩu mới",
                example = "newStrongPassword456"
        )
        String newPassword,

        @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
        @Schema(
                description = "Xác nhận lại mật khẩu mới",
                example = "newStrongPassword456"
        )
        String confirmNewPassword
) {}
