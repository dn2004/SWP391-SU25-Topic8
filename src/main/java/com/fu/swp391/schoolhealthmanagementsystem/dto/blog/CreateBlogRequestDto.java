package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateBlogRequestDto(
        @NotEmpty(message = "Title is required")
        String title,

        @NotEmpty(message = "Thumbnail URL is required")
        @Pattern(regexp = "^https://.*", message = "Thumbnail phải là URL hợp lệ bắt đầu bằng https://")
        String thumbnail,

        @NotEmpty(message = "Description is required")
        String description,

        @NotEmpty(message = "Content is required")
        String content,

        @NotNull(message = "Status is required")
        BlogStatus status,

        @NotNull(message = "Category is required")
        BlogCategory category
) {}
