package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO để cập nhật trạng thái của một bản ghi bệnh mãn tính")
public record ChronicDiseaseStatusUpdateRequestDto(
        @Schema(description = "Trạng thái mới của bản ghi.",
                example = "APPROVE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Trạng thái mới không được để trống.")
        StudentChronicDiseaseStatus newStatus,

        @Schema(description = "Ghi chú của người duyệt (ví dụ: lý do từ chối, thông tin xác minh). Tối đa 500 ký tự.",
                example = "Đã xác minh thông tin với bệnh án của học sinh.")
        @Size(max = 500, message = "Ghi chú của người duyệt không được vượt quá 500 ký tự.")
        String approverNotes
) {
}

