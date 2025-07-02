package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;

public record UpdateBlogRequestDto(
        String title,
        String content,
        BlogStatus status,
        BlogCategory category
) {
}
