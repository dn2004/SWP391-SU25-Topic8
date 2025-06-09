package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Schema(description = "Loại mối quan hệ giữa phụ huynh và học sinh",
        allowableValues = {"FATHER", "MOTHER", "GUARDIAN", "GRANDFATHER", "GRANDMOTHER", "OTHER"})
@Slf4j
public enum RelationshipType {
    FATHER("Bố"),
    MOTHER("Mẹ"),
    GUARDIAN("Người giám hộ"),
    GRANDFATHER("Ông"),
    GRANDMOTHER("Bà"),
    OTHER("Khác");

    private final String vietnameseName;

    RelationshipType(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    // Phương thức này sẽ được converter sử dụng để chuyển từ String (đọc từ DB) về Enum
    public static RelationshipType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return OTHER;
        }
        for (RelationshipType b : RelationshipType.values()) {
            if (b.vietnameseName.equalsIgnoreCase(text)) {
                return b;
            }
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Nếu không tìm thấy, trả về OTHER như logic hiện tại của bạn
        log.warn("Không tìm thấy RelationshipType cho giá trị: {}. Trả về OTHER.", text);
        return OTHER;
    }

    // Lombok @Getter đã tạo getVietnameseName()
}