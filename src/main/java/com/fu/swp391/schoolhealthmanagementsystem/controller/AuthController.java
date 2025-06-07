package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.auth.*;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.AuthService;
import com.google.firebase.auth.FirebaseAuthException;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Đăng ký tài khoản cho Phụ huynh")
    public ResponseEntity<UserDto> registerParent(@Valid @RequestBody RegisterParentRequestDto requestDto) {
        log.info("API: Yêu cầu đăng ký phụ huynh - email: {}", requestDto.email());
        UserDto registeredUser = authService.registerParent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập bằng Email và Mật khẩu")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("API: Yêu cầu đăng nhập - email: {}", requestDto.email());
        LoginResponseDto loginResponse = authService.login(requestDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login/firebase")
    @Operation(summary = "Đăng nhập/Đăng ký bằng Firebase Google Sign-In")
    public ResponseEntity<LoginResponseDto> loginWithFirebase(@Valid @RequestBody FirebaseLoginRequestDto requestDto) throws FirebaseAuthException {
        log.info("API: Yêu cầu đăng nhập bằng Firebase");
        LoginResponseDto loginResponse = authService.loginWithFirebase(requestDto);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất người dùng")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        log.info("API: Yêu cầu đăng xuất");
        authService.logout(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Yêu cầu OTP để đặt lại mật khẩu")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto requestDto) {
        log.info("API: Yêu cầu quên mật khẩu - email: {}", requestDto.email());
        authService.forgotPassword(requestDto);
        // Return 200 OK even if email doesn't exist to prevent email enumeration
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu bằng OTP")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDto requestDto) {
        log.info("API: Yêu cầu đặt lại mật khẩu - email: {}", requestDto.email());
        authService.resetPassword(requestDto);
        return ResponseEntity.ok().build();
    }
}