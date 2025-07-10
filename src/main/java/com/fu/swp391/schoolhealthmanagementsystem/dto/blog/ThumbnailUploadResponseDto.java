package com.fu.swp391.schoolhealthmanagementsystem.dto.blog;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response khi upload thumbnail thành công")
public record ThumbnailUploadResponseDto(
        @Schema(
                description = "URL của thumbnail đã upload",
                example = "https://res.cloudinary.com/mycloud/image/upload/v1234567890/blog-thumbnails/thumbnail_abc123.jpg"
        )
        String thumbnailUrl,

        @Schema(
                description = "Public ID của thumbnail trên Cloudinary",
                example = "blog-thumbnails/thumbnail_abc123"
        )
        String publicId,

        @Schema(
                description = "Thông báo",
                example = "Thumbnail đã được upload thành công"
        )
        String message
) {
}
