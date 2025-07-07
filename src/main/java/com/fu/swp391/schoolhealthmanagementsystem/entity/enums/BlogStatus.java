package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái của bài đăng blog")
public enum BlogStatus {
    PUBLIC("Công khai", "#389e0d", "#f6ffed"), // Xanh lá Ant Design
    PRIVATE("Riêng tư", "#cf1322", "#fff1f0"); // Đỏ Ant Design

    private final String displayName;
    private final String color;
    private final String backgroundColor;

    BlogStatus(String displayName, String color, String backgroundColor) {
        this.displayName = displayName;
        this.color = color;
        this.backgroundColor = backgroundColor;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getBackgroundColor() {
        return backgroundColor;
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
