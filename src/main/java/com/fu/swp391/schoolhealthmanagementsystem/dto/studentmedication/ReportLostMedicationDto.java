package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin báo cáo thất lạc thuốc")
public record ReportLostMedicationDto(
        @Schema(description = "Số liều thuốc bị thất lạc")
        @NotNull(message = "Số liều thuốc bị thất lạc không được để trống")
        Integer dosesLost,
        @Schema(description = "Ghi chú của nhân viên y tế về việc thất lạc thuốc")
        @Size(max=500, message = "Ghi chú không quá 500 ký tự")
        String staffNotes
) {}