package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Trạng thái của một lịch trình cho uống thuốc")
public enum ScheduledMedicationTaskStatus {
    SCHEDULED("Đã lên lịch"),
    ADMINISTERED("Đã cho uống"),
    SKIPPED_SUPPLY_ISSUE("Bỏ qua - Vấn đề thuốc"), // Ví dụ: thuốc thất lạc, hết, hỏng, không đủ số lượng
    SKIPPED_STUDENT_ABSENT("Bỏ qua - Học sinh vắng"),
    SKIPPED_STUDENT_REFUSED("Bỏ qua - Học sinh từ chối"),
    UPDATED_TO_ANOTHER_RECORD("Đã cập nhật lịch trình khác"), // Khi lịch trình này được cập nhật sang một lịch trình khác
    SKIPPED_SYSTEM_OVERDUE("Bỏ qua - Quá hạn xử lý"),
    SKIPPED_MEDICATION_CANCELED("Bỏ qua - Thuốc đã hủy"),
    NOT_ADMINISTERED_OTHER("Không cho uống - Lý do khác"); // Ghi rõ trong notes

    private final String displayName;

    ScheduledMedicationTaskStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static ScheduledMedicationTaskStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (ScheduledMedicationTaskStatus status : ScheduledMedicationTaskStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName) || status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy ScheduledMedicationTaskStatus với displayName: " + displayName);
    }
}