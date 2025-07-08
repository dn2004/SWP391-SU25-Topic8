package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidSkipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin yêu cầu bỏ qua nhiệm vụ uống thuốc đã lên lịch")
public record SkipMedicationTaskRequestDto(

        @Schema(description = "Lý do bỏ qua nhiệm vụ uống thuốc đã lên lịch (trạng thái skip)")
        @NotNull(message = "Lý do bỏ qua không được để trống")
        @ValidSkipStatus
        ScheduledMedicationTaskStatus skipReasonStatus,
        @Schema(description = "Ghi chú chi tiết của nhân viên y tế về lý do bỏ qua")
        @Size(max = 1000, message = "Ghi chú của nhân viên y tế không quá 1000 ký tự")
        String staffNotes // Ghi chú chi tiết hơn về lý do bỏ qua
) {
}