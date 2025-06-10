package com.fu.swp391.schoolhealthmanagementsystem.prop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "file")
@Validated
public record FileStorageProperties(
        @NotEmpty
        List<String> allowedTypes,
        long maxProofSizeBytes
) {}