package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu tạo theo dõi sau tiêm chủng tại trường")
public record CreatePostVaccinationMonitoringRequestDto(
    @Schema(
            description = "ID tiêm chủng tại trường",
            example = "123"
    )
    @NotNull(message = "ID tiêm chủng không được để trống")
    Long schoolVaccinationId,

    @Schema(
            description = "Nhiệt độ (°C)",
            example = "36.8"
    )
    @Min(
            value = 35,
            message = "Nhiệt độ không được thấp hơn 35°C"
    )
    @Max(
            value = 42,
            message = "Nhiệt độ không được cao hơn 42°C"
    )
    Float temperature,

    @Schema(
            description = "Có phản ứng phụ hay không",
            example = "false"
    )
    @NotNull(message = "Thông tin về phản ứng phụ không được để trống")
    Boolean hasSideEffects,

    @Schema(
            description = "Mô tả phản ứng phụ",
            example = "Đau tại chỗ tiêm, hơi mệt"
    )
    @Size(
            max = 1000,
            message = "Mô tả không quá 1000 ký tự"
    )
    String sideEffectsDescription,

    @Schema(
            description = "Hành động đã thực hiện",
            example = "Cho học sinh nghỉ ngơi tại phòng y tế"
    )
    @Size(
            max = 1000,
            message = "Hành động đã thực hiện không quá 1000 ký tự"
    )
    String actionsTaken,

    @Schema(
            description = "Ghi chú bổ sung",
            example = "Học sinh tỉnh táo, không có dấu hiệu bất thường"
    )
    @Size(
            max = 1000,
            message = "Ghi chú không quá 1000 ký tự"
    )
    String notes
) {}
