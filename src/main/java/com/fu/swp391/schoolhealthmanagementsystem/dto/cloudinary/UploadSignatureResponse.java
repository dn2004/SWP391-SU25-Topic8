package com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record UploadSignatureResponse(
        @Schema(description = "The timestamp (in seconds since the Unix epoch) at which the signature was generated.", example = "1678886400")
        long timestamp,
        @Schema(description = "The generated signature to authenticate the upload request.", example = "a1b2c3d4e5f6...")
        String signature,
        @Schema(description = "The API key of your Cloudinary account.", example = "123456789012345")
        String apiKey,
        @Schema(description = "The cloud name of your Cloudinary account.", example = "my-cloud")
        String cloudName,
        @Schema(description = "The target folder in Cloudinary where the file will be uploaded.", example = "school-health/avatars")
        String folder
) {}
