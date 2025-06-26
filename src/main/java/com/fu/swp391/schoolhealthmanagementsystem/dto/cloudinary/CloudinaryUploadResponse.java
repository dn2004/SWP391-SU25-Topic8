package com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary;

import lombok.Builder;

@Builder
public record CloudinaryUploadResponse(
        String url,          // URL an toàn để truy cập file
        String publicId,     // public_id của file trên Cloudinary (dùng để xóa/biến đổi)
        String resourceType, // Loại tài nguyên (image, video, raw)
        String originalFilename, // Tên file gốc từ client
        String format,      // Định dạng file sau khi upload (ví dụ: jpg, png, pdf)
        String contentType // MIME type: image/jpeg, application/pdf
        ) {
}