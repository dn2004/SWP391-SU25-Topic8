package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.user.ChangePasswordRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.ConfirmFullNameRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.SetPasswordRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs quản lý thông tin cá nhân người dùng")
@SecurityRequirement(name = "bearerAuth") // Requires authentication for all endpoints here
@Slf4j
public class UserController {

    private final UserService userService; // You'll need to create/update UserService

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại")
    public ResponseEntity<UserDto> getCurrentUser() {
        log.info("API: Yêu cầu lấy thông tin người dùng hiện tại");
        UserDto userDto = userService.getCurrentUserDto();
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/set-password")
    @Operation(summary = "Đặt mật khẩu mới (cho người dùng đăng nhập qua Google hoặc lần đầu đặt)")
    public ResponseEntity<Void> setPassword(@Valid @RequestBody SetPasswordRequestDto requestDto) {
        log.info("API: Yêu cầu đặt mật khẩu mới");
        userService.setPassword(requestDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    @Operation(summary = "Thay đổi mật khẩu hiện tại")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto) {
        log.info("API: Yêu cầu thay đổi mật khẩu");
        userService.changePassword(requestDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/confirm-fullname") // Hoặc @PostMapping
    @Operation(summary = "Xác nhận hoặc cập nhật tên đầy đủ của người dùng")
    public ResponseEntity<UserDto> confirmOrUpdateFullName(@Valid @RequestBody ConfirmFullNameRequestDto requestDto) {
        UserDto updatedUserDto = userService.confirmOrUpdateFullName(requestDto);
        return ResponseEntity.ok(updatedUserDto);
    }

    // Add other profile update endpoints here (e.g., update full name, phone number)
}