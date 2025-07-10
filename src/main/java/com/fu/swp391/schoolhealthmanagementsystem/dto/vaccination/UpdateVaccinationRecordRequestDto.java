package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu cập nhật hồ sơ tiêm chủng tại trường")
public record UpdateVaccinationRecordRequestDto(
    @Schema(
            description = "Trạng thái tiêm chủng mới",
            example = "Đã hoàn thành",
            allowableValues = {
                    "Đã hoàn thành",
                    "Vắng mặt",
                    "Từ chối"
            }

    )
    @NotNull(message = "Trạng thái tiêm chủng không được để trống")
    SchoolVaccinationStatus status,

    @Schema(
            description = "Ghi chú cập nhật",
            example = "Cập nhật: Học sinh đã tiêm sau khi đến muộn"
    )
    @Size(
            max = 500,
            message = "Ghi chú không quá 500 ký tự"
    )
    String notes,

    @Schema(
            description = "Lý do thay đổi trạng thái",
            example = "Học sinh đến muộn nhưng vẫn được tiêm"
    )
    @Size(
            max = 200,
            message = "Lý do thay đổi không quá 200 ký tự"
    )
    String reasonForChange
) {}
