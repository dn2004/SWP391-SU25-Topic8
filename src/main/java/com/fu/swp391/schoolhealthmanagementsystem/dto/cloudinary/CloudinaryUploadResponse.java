package com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary;

import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "DTO phản hồi cho việc upload file lên Cloudinary")
@Builder
public record CloudinaryUploadResponse(
        @Schema(description = "URL an toàn để truy cập file đã upload", example = "https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg")
        String url,
        @Schema(description = "public_id của file trên Cloudinary", example = "sample")
        String publicId,
        @Schema(description = "Loại tài nguyên (image, video, raw)", example = "image")
        String resourceType,
        @Schema(description = "Tên file gốc từ client", example = "myphoto.jpg")
        String originalFilename,
        @Schema(description = "Định dạng file sau khi upload", example = "jpg")
        String format,
        @Schema(description = "MIME type của file", example = "image/jpeg")
        String contentType
) { }