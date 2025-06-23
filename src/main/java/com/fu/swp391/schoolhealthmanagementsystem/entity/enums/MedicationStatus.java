package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái của thuốc được gửi tại trường")
public enum MedicationStatus {

    AVAILABLE("Sẵn có"),
    OUT_OF_DOSES("Hết liều"),
    EXPIRED("Đã hết hạn"),
    RETURNED_TO_PARENT("Đã trả lại phụ huynh"),
    LOST("Bị thất lạc"),
    CANCEL("Hủy bỏ");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static MedicationStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (MedicationStatus status : MedicationStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy MedicationStatus với displayName: " + displayName);
    }

    MedicationStatus(String displayName) {
        this.displayName = displayName;
    }
}