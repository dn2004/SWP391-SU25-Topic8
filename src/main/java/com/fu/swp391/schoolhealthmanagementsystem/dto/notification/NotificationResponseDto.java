package com.fu.swp391.schoolhealthmanagementsystem.dto.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Đối tượng dữ liệu trả về cho thông báo")
public record NotificationResponseDto(
    @Schema(description = "ID của thông báo")
    Long id,
    @Schema(description = "Nội dung thông báo")
    String content,
    @Schema(description = "Trạng thái đã đọc")
    boolean isRead,
    @Schema(description = "Đường dẫn liên kết khi nhấp vào")
    String link,
    @Schema(description = "Thời gian tạo")
    LocalDateTime createdAt
) {}

