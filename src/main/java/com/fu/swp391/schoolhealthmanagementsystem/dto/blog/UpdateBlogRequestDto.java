package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Yêu cầu cập nhật blog")
public record UpdateBlogRequestDto(

        @Schema(description = "Tiêu đề blog", example = "Healthy Eating Tips")
        String title,

        @Schema(description = "URL ảnh thumbnail", example = "https://example.com/image.jpg")
        String thumbnail,

        @Schema(description = "Mô tả ngắn gọn về blog", example = "Những lời khuyên về ăn uống lành mạnh cho học sinh")
        String description,

        @Schema(description = "Nội dung chi tiết của blog", example = "Ăn nhiều rau xanh, hạn chế đồ ngọt...")
        String content,

        @Schema(example = "Công khai")
        BlogStatus status,

        @Schema(example = "Sơ cấp cứu")
        BlogCategory category
) {
}
