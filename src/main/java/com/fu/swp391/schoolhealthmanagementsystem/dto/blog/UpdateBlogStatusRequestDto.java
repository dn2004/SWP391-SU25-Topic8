package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateBlogStatusRequestDto(
        @Schema(example = "Công khai")
        @NotNull(message = "Trạng thái là bắt buộc")
        BlogStatus status
) {
}

