package com.fu.swp391.schoolhealthmanagementsystem.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record NotificationResponseDto(
        @Schema(
                description = "ID của thông báo",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Nội dung của thông báo",
                example = "Xin chào!"
        )
        String content,

        @Schema(
                description = "Trạng thái đã đọc",
                example = "false"
        )
        boolean read,

        @Schema(
                description = "Đường dẫn đến trang chi tiết",
                example = "/some-link/123"
        )
        String link,

        @Schema(
                description = "Thời gian tạo thông báo",
                example = "2025-07-09T10:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Định danh của người gửi (email hoặc system)",
                example = "system"
        )
        String fromUser,

        @Schema(
                description = "Tên đầy đủ của người gửi",
                example = "Admin"
        )
        String fromUserFullName
) {}
