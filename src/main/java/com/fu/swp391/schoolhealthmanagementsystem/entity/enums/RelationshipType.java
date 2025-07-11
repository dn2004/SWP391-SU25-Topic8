package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Loại mối quan hệ giữa phụ huynh và học sinh")
public enum RelationshipType {
    FATHER("Bố"),
    MOTHER("Mẹ"),
    GUARDIAN("Người giám hộ"),
    GRANDFATHER("Ông"),
    GRANDMOTHER("Bà"),
    OTHER("Khác");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static RelationshipType fromDisplayName(String displayName) {
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        String trimmedDisplayName = displayName.trim();
        for (RelationshipType type : RelationshipType.values()) {
            if (type.displayName.equalsIgnoreCase(trimmedDisplayName) || type.name().equalsIgnoreCase(trimmedDisplayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy RelationshipType với displayName: " + displayName);
    }

    RelationshipType(String displayName) {
        this.displayName = displayName;
    }
}