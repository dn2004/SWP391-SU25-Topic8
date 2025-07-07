package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;

import java.time.LocalDateTime;

public record BlogResponseDto(
        Long id,
        String title,
        String slug,
        String thumbnail,
        String description,
        String content,
        String authorName,
        BlogStatus status,
        BlogCategory category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}