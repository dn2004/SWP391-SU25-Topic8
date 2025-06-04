package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phản hồi đăng nhập thành công")
public record LoginResponseDto(
        @Schema(description = "JWT token để xác thực các yêu cầu tiếp theo")
        String accessToken,

        @Schema(description = "Thông tin người dùng")
        UserDto user
) {}
