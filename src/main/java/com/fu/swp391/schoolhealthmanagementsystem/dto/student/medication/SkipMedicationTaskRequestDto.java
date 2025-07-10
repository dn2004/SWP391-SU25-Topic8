package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidSkipStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin yêu cầu bỏ qua nhiệm vụ uống thuốc đã lên lịch")
public record SkipMedicationTaskRequestDto(
        @Schema(
                description = "Lý do bỏ qua nhiệm vụ uống thuốc đã lên lịch (trạng thái skip)",
                example = "Bỏ qua - Vấn đề thuốc",
                allowableValues = {
                        "Bỏ qua - Vấn đề thuốc",
                        "Bỏ qua - Học sinh vắng",
                        "Bỏ qua - Học sinh từ chối",
                }
        )
        @NotNull(message = "Lý do bỏ qua không được để trống")
        @ValidSkipStatus
        ScheduledMedicationTaskStatus skipReasonStatus,

        @Schema(
                description = "Ghi chú chi tiết của nhân viên y tế về lý do bỏ qua",
                example = "Học sinh vắng mặt do nghỉ bệnh"
        )
        @Size(
                max = 1000,
                message = "Ghi chú của nhân viên y tế không quá 1000 ký tự"
        )
        String staffNotes
) {}
