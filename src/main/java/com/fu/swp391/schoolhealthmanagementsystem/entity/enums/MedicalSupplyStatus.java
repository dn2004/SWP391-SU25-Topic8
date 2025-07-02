package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái của vật tư y tế")
public enum MedicalSupplyStatus {

    AVAILABLE("Sẵn có"),
    OUT_OF_STOCK("Hết hàng"),
    EXPIRED("Hết hạn"),
    DISPOSE("Đã loại bỏ ra khỏi kho");

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static MedicalSupplyStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            return null;
        }

        for (MedicalSupplyStatus status : MedicalSupplyStatus.values()) {
            if (status.getDisplayName().equalsIgnoreCase(displayName) ||
                status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái vật tư y tế cho giá trị: " + displayName);
    }

    MedicalSupplyStatus(String displayName) {
        this.displayName = displayName;
    }
}
