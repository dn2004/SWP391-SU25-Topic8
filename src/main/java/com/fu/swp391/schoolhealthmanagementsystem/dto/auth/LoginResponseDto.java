package com.fu.swp391.schoolhealthmanagementsystem.dto.auth;

import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Phản hồi đăng nhập")
public record LoginResponseDto(
        @Schema(
                description = "Token truy cập",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        )
        String accessToken,

        @Schema(description = "Thông tin người dùng",
//                implementation = UserDto.class,
                example =
                """
                {
                        "userId": 1,
                        "fullName": "Nguyễn Văn A",
                        "email": "user@example.com",
                        "phoneNumber": "0901234567",
                        "role": "STAFF_MANAGER",
                        "isActive": true,
                        "linkedToStudent": false
                }
                """
        )
        UserDto user
) {
}
