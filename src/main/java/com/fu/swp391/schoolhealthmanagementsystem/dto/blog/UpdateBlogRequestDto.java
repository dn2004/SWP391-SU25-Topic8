package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import jakarta.validation.constraints.Pattern;

public record UpdateBlogRequestDto(
        String title,

        @Pattern(regexp = "^https://.*", message = "Thumbnail phải là URL hợp lệ bắt đầu bằng https://")
        String thumbnail,

        String description,
        String content,
        BlogStatus status,
        BlogCategory category
) {
}
