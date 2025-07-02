package com.fu.swp391.schoolhealthmanagementsystem.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Đối tượng chuyển dữ liệu thông báo")
public record NotificationDTO(
        @Schema(description = "Nội dung của thông báo", example = "Xin chào!")
        String content,
        @Schema(description = "Định danh của người dùng nhận", example = "user123")
        String toUser,
        @Schema(description = "Định danh của người gửi (có thể là 'system' hoặc username)", example = "system")
        String fromUser,
        @Schema(description = "Đường dẫn đến trang chi tiết", example = "/some-link/123")
        String link
) {
}
