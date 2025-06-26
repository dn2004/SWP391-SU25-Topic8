package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;

import java.time.LocalDateTime;

public record BlogResponseDto(
        Long id,
        String title,
        String content,
        String authorName,
        BlogStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}