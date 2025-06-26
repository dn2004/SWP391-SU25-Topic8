package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateBlogRequestDto(
        @NotEmpty(message = "Title is required")
        String title,

        @NotEmpty(message = "Content is required")
        String content,

        @NotNull(message = "Status is required")
        BlogStatus status
) {
}
