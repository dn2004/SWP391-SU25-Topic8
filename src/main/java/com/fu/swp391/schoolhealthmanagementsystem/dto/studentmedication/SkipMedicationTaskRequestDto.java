package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidSkipStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SkipMedicationTaskRequestDto(

        @NotNull(message = "Lý do bỏ qua không được để trống")
        @ValidSkipStatus
        ScheduledMedicationTaskStatus skipReasonStatus,
        @Size(max = 1000, message = "Ghi chú của nhân viên y tế không quá 1000 ký tự")
        String staffNotes // Ghi chú chi tiết hơn về lý do bỏ qua
) {
}