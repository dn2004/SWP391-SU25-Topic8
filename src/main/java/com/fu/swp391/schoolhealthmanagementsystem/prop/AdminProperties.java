package com.fu.swp391.schoolhealthmanagementsystem.prop;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.admin") // Giữ nguyên prefix
@Validated
public record AdminProperties(
        @NotBlank String
        fullName,
        @NotBlank String
        password,
        @NotBlank @Email
        String email

) {}