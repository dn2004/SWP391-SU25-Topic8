package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordVaccinationRequestDto(
    @Schema(description = "ID của phiếu đồng ý", example = "123")
    @NotNull(message = "ID phiếu đồng ý không được để trống")
    Long consentId,

    @Schema(description = "Trạng thái tiêm chủng (COMPLETED, ABSENT, DECLINED)", example = "COMPLETED")
    @NotNull(message = "Trạng thái tiêm chủng không được để trống")
    SchoolVaccinationStatus status,

    @Schema(description = "Ghi chú", example = "Học sinh không có phản ứng sau tiêm")
    @Size(max = 500, message = "Ghi chú không quá 500 ký tự")
    String notes
) {}
