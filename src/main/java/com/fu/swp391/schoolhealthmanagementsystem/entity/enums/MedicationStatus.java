package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

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

    MedicationStatus(String displayName) {
        this.displayName = displayName;
    }
}