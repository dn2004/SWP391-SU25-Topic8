package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin báo cáo thất lạc thuốc (yêu cầu)")
public record ReportLostMedicationRequestDto(
        @Schema(
                description = "Ghi chú của nhân viên y tế về việc thất lạc thuốc",
                example = "Thuốc bị rơi khi di chuyển giữa các lớp"
        )
        @Size(
                max = 500,
                message = "Ghi chú về việc thất lạc không quá 500 ký tự"
        )
        String staffNotes // Lý do, hoàn cảnh thất lạc, ai phát hiện,...
) {}
