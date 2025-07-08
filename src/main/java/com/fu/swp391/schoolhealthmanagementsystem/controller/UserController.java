package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.user.ChangePasswordRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "APIs quản lý thông tin cá nhân người dùng")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
        summary = "Lấy thông tin người dùng hiện tại",
        description = """
### Mô tả
Lấy thông tin chi tiết của người dùng đang đăng nhập.
- **Phân quyền:** Yêu cầu người dùng đã xác thực.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<UserDto> getCurrentUser() {
        log.info("API: Yêu cầu lấy thông tin người dùng hiện tại");
        UserDto userDto = userService.getCurrentUserDto();
        return ResponseEntity.ok(userDto);
    }

    @PutMapping("/change-password")
    @Operation(
        summary = "Thay đổi mật khẩu hiện tại",
        description = """
### Mô tả
Thay đổi mật khẩu cho tài khoản đang đăng nhập. Yêu cầu nhập mật khẩu cũ.
- **Phân quyền:** Yêu cầu người dùng đã xác thực.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thay đổi mật khẩu thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc mật khẩu cũ không chính xác", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequestDto requestDto) {
        log.info("API: Yêu cầu thay đổi mật khẩu");
        userService.changePassword(requestDto);
        return ResponseEntity.ok().build();
    }
}