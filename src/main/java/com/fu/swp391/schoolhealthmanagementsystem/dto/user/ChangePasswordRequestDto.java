package com.fu.swp391.schoolhealthmanagementsystem.dto.user;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches; // Import custom annotation
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đổi mật khẩu hiện tại")
// Áp dụng @PasswordMatches, chỉ định các trường cần so sánh là newPassword và confirmNewPassword
@PasswordMatches(passwordField = "newPassword", confirmPasswordField = "confirmNewPassword")
public class ChangePasswordRequestDto {

    @NotBlank(message = "Mật khẩu cũ không được để trống")
    @Schema(description = "Mật khẩu hiện tại của người dùng", example = "currentPassword123")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu mới phải từ 6 đến 255 ký tự")
    @Schema(description = "Mật khẩu mới", example = "newStrongPassword456")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    @Schema(description = "Xác nhận lại mật khẩu mới", example = "newStrongPassword456")
    private String confirmNewPassword; // Trường mới để xác nhận mật khẩu mới
}