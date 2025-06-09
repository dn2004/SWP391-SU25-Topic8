package com.fu.swp391.schoolhealthmanagementsystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties; // Có thể cần nếu không dùng @Component
import org.springframework.context.annotation.Configuration; // Có thể cần nếu dùng @EnableConfigurationProperties ở đây

import java.util.List;

// Cách 1: Vẫn để @Component (hoạt động, nhưng ít "record-like" hơn)
// @Component
// @ConfigurationProperties(prefix = "file")
// public record FileStorageProperties(
//         List<String> allowedTypes,
//         long maxProofSizeBytes // Tên trường nên là camelCase để khớp với properties
// ) {
// }

// Cách 2: Sử dụng @EnableConfigurationProperties (Khuyến nghị cho Record)
// Bỏ @Component khỏi record và kích hoạt nó trong một lớp @Configuration riêng
// (Ví dụ: trong AppPropertiesConfiguration đã tạo ở ví dụ trước, hoặc tạo mới)

@ConfigurationProperties(prefix = "file")
public record FileStorageProperties(
        List<String> allowedTypes,
        // Tên thuộc tính trong file properties: file.max-proof-size-bytes
        // Spring Boot sẽ tự động map kebab-case (max-proof-size-bytes)
        // hoặc camelCase (maxProofSizeBytes) vào trường này.
        long maxProofSizeBytes
) {
    // Record tự động tạo constructor, getters, equals, hashCode, toString.

    // Nếu bạn muốn giá trị mặc định khi không có trong file properties:
    // Với List, bạn có thể khởi tạo một List rỗng trong constructor tùy chỉnh nếu cần,
    // hoặc Spring Boot sẽ inject một List rỗng nếu key không tồn tại.
    // Với long, nó sẽ là 0 nếu không được cung cấp.
    // Ví dụ về constructor tùy chỉnh để set default (ít phổ biến cho config properties đơn giản):
    /*
    public FileStorageProperties {
        if (allowedTypes == null) {
            allowedTypes = List.of("image/jpeg", "image/png", "application/pdf"); // Default
        }
        if (maxProofSizeBytes == 0) {
            maxProofSizeBytes = 5 * 1024 * 1024; // 5MB default
        }
    }
    */
    // Tuy nhiên, cách tốt hơn để đặt giá trị mặc định là trong file application.properties
    // file.allowed-types=image/jpeg,image/png,application/pdf
    // file.max-proof-size-bytes=5242880
    // Hoặc, nếu key không tồn tại, bạn kiểm tra và gán default trong code sử dụng nó.
}