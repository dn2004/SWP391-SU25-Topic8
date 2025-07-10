package com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
@Builder
public record UploadSignatureResponse(
        @Schema(
                description = "Dấu thời gian (tính bằng giây kể từ thời điểm Unix epoch) khi chữ ký được tạo ra.",
                example = "1678886400")
        long timestamp,
        @Schema(
                description = "Chữ ký được tạo ra để xác thực yêu cầu tải lên.",
                example = "a1b2c3d4e5f6..."
        )
        String signature,
        @Schema(
                description = "API key của tài khoản Cloudinary của bạn.",
                example = "123456789012345"
        )
        String apiKey,
        @Schema(
                description = "Tên cloud của tài khoản Cloudinary của bạn.",
                example = "my-cloud"
        )
        String cloudName,
        @Schema(
                description = "Thư mục đích trong Cloudinary nơi tệp sẽ được tải lên.",
                example = "school-health/avatars"
        )
        String folder
) {}