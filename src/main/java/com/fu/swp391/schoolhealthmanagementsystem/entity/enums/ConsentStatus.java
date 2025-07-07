    package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái phiếu đồng ý tiêm chủng")
public enum ConsentStatus {
    PENDING("Đang chờ"),    // No response from parent yet
    APPROVED("Đồng ý"),     // Parent has approved vaccination
    DECLINED("Từ chối");    // Parent has declined vaccination
    private final String displayName;

    ConsentStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ConsentStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (ConsentStatus status : ConsentStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName) || status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy ConsentStatus với displayName: " + displayName);
    }
}
