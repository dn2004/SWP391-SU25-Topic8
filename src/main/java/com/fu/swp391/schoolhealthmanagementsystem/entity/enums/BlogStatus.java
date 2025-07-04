package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái của bài đăng blog")
public enum BlogStatus {
    PUBLIC("Công khai", "#4CAF50"), // Xanh lá
    PRIVATE("Riêng tư", "#f44336"); // Đỏ

    private final String displayName;
    private final String backgroundColor;

    BlogStatus(String displayName, String backgroundColor) {
        this.displayName = displayName;
        this.backgroundColor = backgroundColor;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static BlogStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        for (BlogStatus status : BlogStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName) || status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái với tên hiển thị: " + displayName);
    }
}
