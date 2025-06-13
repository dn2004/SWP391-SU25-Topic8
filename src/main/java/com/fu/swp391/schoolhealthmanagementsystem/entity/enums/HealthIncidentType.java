package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

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

    public String getDisplayName() {
        return displayName;
    }

    public static HealthIncidentType fromString(String text) {
        for (HealthIncidentType b : HealthIncidentType.values()) {
            if (b.name().equalsIgnoreCase(text) || b.displayName.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return OTHER;
    }
}