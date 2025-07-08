package com.fu.swp391.schoolhealthmanagementsystem.prop;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.frontend")
@Validated
public record FrontEndProperties(
    @NotBlank String baseUrl,
    @NotBlank String loginPath
) {}

