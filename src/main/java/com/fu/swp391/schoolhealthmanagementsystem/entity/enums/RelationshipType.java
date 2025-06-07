package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Loại mối quan hệ giữa phụ huynh và học sinh",
        allowableValues = {"FATHER", "MOTHER", "GUARDIAN", "GRANDFATHER", "GRANDMOTHER", "BROTHER", "SISTER", "OTHER"})
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


    public static RelationshipType fromString(String text) {
        for (RelationshipType b : RelationshipType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.vietnameseName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Có thể ném IllegalArgumentException nếu không tìm thấy, hoặc trả về null/OTHER
        return OTHER; // Hoặc ném lỗi
    }
}