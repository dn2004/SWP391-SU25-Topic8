package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SchoolSession {
    MORNING("Sáng"),
    AFTERNOON("Chiều");

    private final String displayName;

    SchoolSession(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SchoolSession fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (SchoolSession session : SchoolSession.values()) {
            if (session.displayName.equalsIgnoreCase(displayName) || session.name().equalsIgnoreCase(displayName)) {
                return session;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy SchoolSession với displayName: " + displayName);
    }
}