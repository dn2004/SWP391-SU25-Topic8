package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin trả thuốc về cho phụ huynh (yêu cầu)")
public record ReturnMedicationToParentRequestDto(
        @Schema(
                description = "Ghi chú của nhân viên y tế về việc trả thuốc cho phụ huynh",
                example = "Trả lại thuốc cho phụ huynh vào ngày 2025-07-09"
        )
        @Size(
                max = 500,
                message = "Ghi chú về việc trả thuốc không quá 500 ký tự"
        )
        String staffNotes
) {}