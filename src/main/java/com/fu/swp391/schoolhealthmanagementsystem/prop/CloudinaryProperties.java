package com.fu.swp391.schoolhealthmanagementsystem.prop;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "cloudinary")
@Validated
public record CloudinaryProperties(
        @NotBlank
        String cloudName,
        @NotBlank
        String apiKey,
        @NotBlank
        String apiSecret,
        @NotBlank
        String baseFolder
) {}
