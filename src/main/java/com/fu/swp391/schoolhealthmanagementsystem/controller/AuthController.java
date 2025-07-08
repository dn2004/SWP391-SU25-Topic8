package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.auth.*;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs xác thực người dùng")
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/parent")
    @Operation(
        summary = "Đăng ký tài khoản cho Phụ huynh",
        description = """
### Mô tả
Đăng ký tài khoản mới cho phụ huynh. 
- **Phân quyền:** Công khai, không yêu cầu xác thực.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Đăng ký thành công",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email đã tồn tại", content = @Content)
    })
    public ResponseEntity<UserDto> registerParent(@Valid @RequestBody RegisterParentRequestDto requestDto) {
        log.info("API: Yêu cầu đăng ký phụ huynh - email: {}", requestDto.email());
        UserDto registeredUser = authService.registerParent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    @Operation(
        summary = "Đăng nhập bằng Email và Mật khẩu",
        description = """
### Mô tả
Xác thực người dùng và trả về JWT token.
- **Phân quyền:** Công khai, không yêu cầu xác thực.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "401", description = "Email hoặc mật khẩu không chính xác", content = @Content),
        @ApiResponse(responseCode = "403", description = "Tài khoản đã bị khóa", content = @Content)
    })
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("API: Yêu cầu đăng nhập - email: {}", requestDto.email());
        LoginResponseDto loginResponse = authService.login(requestDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    @Operation(
        summary = "Đăng xuất người dùng",
        description = """
### Mô tả
Đăng xuất và vô hiệu hóa token hiện tại.
- **Phân quyền:** Yêu cầu người dùng đã xác thực (đăng nhập).
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đăng xuất thành công", content = @Content),
        @ApiResponse(responseCode = "401", description = "Token không hợp lệ", content = @Content)
    })
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("API: Yêu cầu đăng xuất");
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(
        summary = "Yêu cầu OTP để đặt lại mật khẩu",
        description = """
### Mô tả
Gửi mã OTP đến email để người dùng có thể bắt đầu quá trình đặt lại mật khẩu.
- **Phân quyền:** Công khai, không yêu cầu xác thực.
- **Thông báo:** Gửi email chứa mã OTP đến người dùng.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP đã được gửi", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "429", description = "Quá nhiều yêu cầu", content = @Content)
    })
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto requestDto) {
        log.info("API: Yêu cầu quên mật khẩu - email: {}", requestDto.email());
        authService.forgotPassword(requestDto);
        // Return 200 OK even if email doesn't exist to prevent email enumeration
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(
        summary = "Đặt lại mật khẩu bằng OTP",
        description = """
### Mô tả
Đặt lại mật khẩu mới bằng mã OTP đã nhận qua email.
- **Phân quyền:** Công khai, không yêu cầu xác thực.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc OTP không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content)
    })
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        log.info("API: Yêu cầu đặt lại mật khẩu - email: {}", requestDto.email());
        authService.resetPassword(requestDto);
        return ResponseEntity.ok().build();
    }
}