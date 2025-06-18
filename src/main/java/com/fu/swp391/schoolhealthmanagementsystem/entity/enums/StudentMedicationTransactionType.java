package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

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
}