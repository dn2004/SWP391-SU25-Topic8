package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Danh mục bài viết")
public record CategoryResponseDto(
    @Schema(
        description = "Giá trị danh mục (enum)",
        example = "HEALTH_NEWS"
    )
    String value,
    @Schema(
        description = "Tên hiển thị của danh mục",
        example = "Tin tức Sức khỏe"
    )
    String displayName,
    @Schema(
        description = "Màu sắc của danh mục",
        example = "#1E88E5"
    )
    String color,
    @Schema(
        description = "Màu nền của danh mục",
        example = "#E3F2FD"
    )
    String backgroundColor
) {}