package com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO để cập nhật trạng thái của một bản ghi tiêm chủng")
public record VaccinationStatusUpdateRequestDto(
        @Schema(
                description = "Trạng thái mới của bản ghi tiêm chủng.",
                example = "Chấp nhận",
                allowableValues = {
                        "Chấp nhận",
                        "Từ chối"
                }
        )
        @NotNull(message = "Trạng thái mới không được để trống.")
        StudentVaccinationStatus newStatus,

        @Schema(
                description = "Ghi chú của người duyệt (ví dụ: lý do từ chối, thông tin xác minh). Tối đa 500 ký tự.",
                example = "Đã xác minh thông tin với sổ tiêm chủng của học sinh."
        )
        @Size(max = 500, message = "Ghi chú của người duyệt không được vượt quá 500 ký tự.")
        String approverNotes
) {}
