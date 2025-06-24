package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema; // Nếu bạn muốn dùng Swagger
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Schema(description = "Trạng thái liên kết")
public enum LinkStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động");

    private final String displayName;

    LinkStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static LinkStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (LinkStatus status : LinkStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName) || status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy LinkStatus với displayName: " + displayName);
    }
}