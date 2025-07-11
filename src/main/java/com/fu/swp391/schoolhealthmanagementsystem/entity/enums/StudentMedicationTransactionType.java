package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Loại giao dịch liên quan đến thuốc của học sinh")
public enum StudentMedicationTransactionType {
    INITIAL_STOCK("Nhập kho ban đầu"),      // Khi NVYT nhận thuốc lần đầu từ PH
    ADMINISTERED("Đã cho uống"),           // Đã cho học sinh uống (xuất kho liều)
    LOST("Thất lạc"),                   // Ghi nhận thuốc bị thất lạc
    EXPIRED_REMOVAL("Loại bỏ do hết hạn"), // Loại bỏ thuốc hết hạn
    RETURNED_TO_PARENT("Trả lại phụ huynh"), // Trả thuốc lại cho phụ huynh
    CANCELLATION_REVERSAL("Hủy thuốc"); // Hủy thuốc đã nhập (thu hồi)

    private final String displayName;

    StudentMedicationTransactionType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static StudentMedicationTransactionType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("display name không được để trống");
        }
        for (StudentMedicationTransactionType type : StudentMedicationTransactionType.values()) {
            if (type.displayName.equalsIgnoreCase(displayName) || type.name().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy StudentMedicationTransactionType với displayName: " + displayName);
    }
}