package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Danh mục của bài đăng blog")
public enum BlogCategory {
    HEALTH_NEWS("Tin tức sức khỏe", "#1E88E5"), // Xanh dương
    NUTRITION("Dinh dưỡng", "#43A047"), // Xanh lá
    MENTAL_HEALTH("Sức khỏe tâm thần", "#8E24AA"), // Tím
    DISEASE_PREVENTION("Phòng ngừa bệnh tật", "#E53935"), // Đỏ
    FIRST_AID("Sơ cấp cứu", "#FB8C00"), // Cam
    PHYSICAL_ACTIVITY("Hoạt động thể chất", "#FFB300"), // Vàng
    GROWTH_DEVELOPMENT("Phát triển và tăng trưởng", "#00ACC1"), // Xanh ngọc
    HEALTH_EDUCATION("Giáo dục sức khỏe", "#5E35B1"), // Tím đậm
    OTHER("Khác", "#757575"); // Xám

    private final String displayName;
    private final String color;

    BlogCategory(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static BlogCategory fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }
        for (BlogCategory category : BlogCategory.values()) {
            if (category.displayName.equalsIgnoreCase(displayName) || category.name().equalsIgnoreCase(displayName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy danh mục với tên hiển thị: " + displayName);
    }
}
