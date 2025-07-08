package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu đăng ký tài khoản cho phụ huynh")
@PasswordMatches
public record RegisterParentRequestDto(

        @NotBlank(message = "Họ và tên không được để trống")
        @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
        @Schema(description = "Họ và tên đầy đủ của phụ huynh", example = "Nguyễn Văn A")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Email tối đa 100 ký tự")
        @Schema(description = "Địa chỉ email của phụ huynh", example = "parent@example.com")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
        @Schema(description = "Mật khẩu đăng nhập", example = "password123")
        String password,

        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        @Schema(description = "Xác nhận lại mật khẩu", example = "password123")
        String confirmPassword,

        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        @Schema(description = "Số điện thoại của phụ huynh (tùy chọn)", example = "0905123456")
        String phoneNumber
) {}
