package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thông tin phản hồi của blog")
public record BlogResponseDto(
        @Schema(description = "ID của blog", example = "1")
        Long id,
        @Schema(description = "Tiêu đề của blog", example = "Healthy Living")
        String title,
        @Schema(description = "Slug của blog", example = "healthy-living")
        String slug,
        @Schema(description = "Ảnh thu nhỏ của blog", example = "thumbnail.jpg")
        String thumbnail,
        @Schema(description = "Mô tả ngắn của blog", example = "A short description")
        String description,
        @Schema(description = "Nội dung của blog", example = "Full blog content")
        String content,
        @Schema(description = "Tên tác giả", example = "John Doe")
        String authorName,
        @Schema(description = "Trạng thái của blog", example = "Công khai")
        BlogStatus status,
        @Schema(description = "Danh mục của blog", example = "Tin tức sức khỏe")
        BlogCategory category,
        @Schema(description = "Ngày tạo", example = "2024-06-01T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "Ngày cập nhật", example = "2024-06-02T12:00:00")
        LocalDateTime updatedAt
) {
}