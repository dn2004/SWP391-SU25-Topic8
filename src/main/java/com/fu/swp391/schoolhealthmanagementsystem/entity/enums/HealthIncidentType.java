package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Loại sự cố sức khỏe")
public enum HealthIncidentType {
    MINOR_INJURY("Chấn thương nhẹ"),
    ILLNESS("Ốm đau"),
    ALLERGIC_REACTION("Phản ứng dị ứng"),
    HEAD_INJURY("Chấn thương đầu"),
    FEVER("Sốt"),
    STOMACH_ACHE("Đau bụng"),
    OTHER("Khác");

    private final String displayName;

    HealthIncidentType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static HealthIncidentType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (HealthIncidentType type : HealthIncidentType.values()) {
            if (type.displayName.equalsIgnoreCase(displayName) || type.name().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy HealthIncidentType với displayName: " + displayName);
    }
}