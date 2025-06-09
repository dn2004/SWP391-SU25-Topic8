package com.fu.swp391.schoolhealthmanagementsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin") // Giữ nguyên prefix
public record AdminProperties(
        String fullName, // Các trường sẽ tự động là final và có getter
        String password,
        String email
) {}