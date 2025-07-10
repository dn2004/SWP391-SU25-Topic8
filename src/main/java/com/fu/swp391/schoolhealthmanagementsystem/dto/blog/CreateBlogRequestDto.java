package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Yêu cầu tạo blog")
public record CreateBlogRequestDto(
        @Schema(
                description = "Tiêu đề blog",
                example = "Cách giữ gìn sức khỏe học đường"
        )
        @NotEmpty(message = "Tiêu đề là bắt buộc")
        String title,

        @Schema(
                description = "Ảnh đại diện blog",
                example = "https://example.com/image.jpg"
        )
        @NotEmpty(message = "Ảnh đại diện là bắt buộc")
        String thumbnail,

        @Schema(
                description = "Mô tả ngắn về blog",
                example = "Bài viết chia sẻ các bí quyết giữ gìn sức khỏe cho học sinh."
        )
        @NotEmpty(message = "Mô tả là bắt buộc")
        String description,

        @Schema(
                description = "Nội dung chi tiết của blog",
                example = "Nội dung bài viết về sức khỏe học đường..."
        )
        @NotEmpty(message = "Nội dung là bắt buộc")
        String content,

        @Schema(
                description = "Trạng thái của blog",
                example = "Công khai"
        )
        @NotNull(message = "Trạng thái là bắt buộc")
        BlogStatus status,

        @Schema(
                description = "Danh mục của blog",
                example = "Tin tức sức khỏe"
        )
        @NotNull(message = "Danh mục là bắt buộc")
        BlogCategory category
) {}