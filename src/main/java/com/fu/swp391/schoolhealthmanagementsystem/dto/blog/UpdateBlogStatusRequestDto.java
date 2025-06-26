package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateBlogStatusRequestDto(
        @NotNull(message = "Status is required")
        BlogStatus status
) {
}

