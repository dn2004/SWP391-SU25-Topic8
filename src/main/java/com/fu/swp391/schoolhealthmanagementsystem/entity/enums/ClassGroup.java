package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * Enum đại diện cho các khối lớp trong trường mầm non
 */
@Getter
public enum ClassGroup {
    MAM("Mầm"), // Khối Mầm
    CHOI("Chồi"), // Khối Chồi
    LA("Lá");   // Khối Lá

    private final String displayName;

    ClassGroup(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ClassGroup fromString(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        String normalizedText = text.toUpperCase().trim();

        // Kiểm tra tên enum trước
        for (ClassGroup group : ClassGroup.values()) {
            if (group.name().equals(normalizedText)) {
                return group;
            }
        }

        // Kiểm tra displayName
        for (ClassGroup group : ClassGroup.values()) {
            // Hỗ trợ tìm kiếm không dấu và có dấu
            if (group.displayName.equalsIgnoreCase(text) ||
                removeAccents(group.displayName).equalsIgnoreCase(removeAccents(text))) {
                return group;
            }
        }

        return null;
    }

    /**
     * Phương thức đơn giản để loại bỏ dấu tiếng Việt phục vụ so sánh
     */
    private static String removeAccents(String text) {
        if (text == null) return null;
        return text.replace('ầ', 'a')
                  .replace('ồ', 'o')
                  .replace('ố', 'o')
                  .replace('á', 'a');
    }
}
