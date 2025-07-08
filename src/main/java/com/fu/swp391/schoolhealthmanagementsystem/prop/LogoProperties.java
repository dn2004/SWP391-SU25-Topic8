package com.fu.swp391.schoolhealthmanagementsystem.prop;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.email.logo")
@Validated
public record LogoProperties(
    @NotBlank String path,
    @NotBlank String cid
) {}

