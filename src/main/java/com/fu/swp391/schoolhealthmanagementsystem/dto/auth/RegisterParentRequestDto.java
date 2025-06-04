package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import com.fu.swp391.schoolhealthmanagementsystem.validation.PasswordMatches; // Import custom annotation
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu đăng ký tài khoản cho phụ huynh")
@PasswordMatches // Áp dụng custom validation ở cấp độ class
public class RegisterParentRequestDto {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên tối đa 100 ký tự")
    @Schema(description = "Họ và tên đầy đủ của phụ huynh", example = "Nguyễn Văn A")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    @Schema(description = "Địa chỉ email của phụ huynh", example = "parent@example.com")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 255, message = "Mật khẩu phải từ 6 đến 255 ký tự")
    @Schema(description = "Mật khẩu đăng nhập", example = "password123")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống") // Thêm validation cho trường này
    @Schema(description = "Xác nhận lại mật khẩu", example = "password123")
    private String confirmPassword; // Trường mới

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Schema(description = "Số điện thoại của phụ huynh (tùy chọn)", example = "0905123456")
    private String phoneNumber;
}