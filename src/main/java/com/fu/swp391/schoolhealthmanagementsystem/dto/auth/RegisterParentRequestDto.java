package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches;
import com.fu.swp391.schoolhealthmanagementsystem.validation.VietnamesePhone;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu đăng ký tài khoản phụ huynh")
@PasswordMatches
public record RegisterParentRequestDto(

        @Schema(
                description = "Họ và tên đầy đủ của phụ huynh",
                example = "Nguyễn Văn A"
        )
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(
                max = 100,
                message = "Họ và tên tối đa 100 ký tự"
        )
        String fullName,

        @Schema(
                description = "Email của phụ huynh",
                example = "parent@example.com"
        )
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(
                max = 100,
                message = "Email tối đa 100 ký tự"
        )
        String email,

        @Schema(
                description = "Mật khẩu của phụ huynh",
                example = "password123"
        )
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(
                min = 6,
                max = 255,
                message = "Mật khẩu phải từ 6 đến 255 ký tự"
        )
        String password,

        @Schema(
                description = "Xác nhận lại mật khẩu",
                example = "password123"
        )
        @NotBlank(message = "Xác nhận mật khẩu không được để trống")
        String confirmPassword,


        @VietnamesePhone
        @Schema(
                description = "Số điện thoại của phụ huynh(tùy chọn)",
                example = "0987654321"
        )
        String phoneNumber
) {
}
