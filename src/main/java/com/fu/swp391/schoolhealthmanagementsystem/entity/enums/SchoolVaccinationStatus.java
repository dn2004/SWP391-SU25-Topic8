package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái tiêm chủng tại trường")
public enum SchoolVaccinationStatus {
    SCHEDULED("Đã lên lịch"),       // Vaccination is scheduled
    COMPLETED("Đã hoàn thành"),     // Vaccination was successfully administered
    ABSENT("Vắng mặt"),             // Student was absent on vaccination day
    DECLINED("Từ chối"),            // Parent declined or student had contraindications at the time
    POST_MONITORING("Đang theo dõi"); // In post-vaccination monitoring period

    private final String displayName;

    SchoolVaccinationStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SchoolVaccinationStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (SchoolVaccinationStatus status : SchoolVaccinationStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName) || status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy SchoolVaccinationStatus với displayName: " + displayName);
    }
}
