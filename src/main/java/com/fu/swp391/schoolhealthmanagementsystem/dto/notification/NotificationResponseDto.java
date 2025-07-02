package com.fu.swp391.schoolhealthmanagementsystem.dto.notification;

import java.time.LocalDateTime;

public record NotificationResponseDto(
        Long id,
        String content,
        boolean read,
        String link,
        LocalDateTime createdAt,
        String fromUser, // email or system
        String fromUserFullName
) {
}
