package com.fu.swp391.schoolhealthmanagementsystem.dto.user;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches; // Import custom annotation
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đặt mật khẩu mới (cho user đăng nhập Google hoặc muốn đổi)")
@PasswordMatches(passwordField = "newPassword", confirmPasswordField = "confirmNewPassword") // Áp dụng và chỉ định tên trường
public class SetPasswordRequestDto {

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu mới phải từ 6 đến 255 ký tự")
    @Schema(description = "Mật khẩu mới", example = "newSecurePassword123")
    private String newPassword;

    @NotBlank(message = "Xác nhận mật khẩu mới không được để trống")
    @Schema(description = "Xác nhận lại mật khẩu mới", example = "newSecurePassword123")
    private String confirmNewPassword; // Trường mới để xác nhận
}