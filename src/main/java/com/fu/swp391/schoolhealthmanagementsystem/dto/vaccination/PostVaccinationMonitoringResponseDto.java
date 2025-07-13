package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

@Schema(description = "Phản hồi theo dõi sau tiêm chủng tại trường")
public record PostVaccinationMonitoringResponseDto(
    @Schema(
        description = "ID theo dõi sau tiêm",
        example = "1"
    )
    Long monitoringId,

    @Schema(
        description = "ID tiêm chủng tại trường",
        example = "1"
    )
    Long schoolVaccinationId,

    @Schema(
        description = "Thời gian theo dõi",
        example = "2024-06-01T09:30:00"
    )
    LocalDateTime monitoringTime,


    @Schema(
        description = "Nhiệt độ (°C)",
        example = "36.7"
    )
    @Min(35)
    @Max(42)
    Float temperature,

    @Schema(
        description = "Có phản ứng phụ hay không",
        example = "true"
    )
    Boolean hasSideEffects,

    @Schema(
        description = "Mô tả phản ứng phụ",
        example = "Đau nhẹ tại chỗ tiêm"
    )
    String sideEffectsDescription,

    @Schema(
        description = "Hành động đã thực hiện",
        example = "Chườm lạnh, theo dõi thêm"
    )
    String actionsTaken,

    @Schema(
        description = "Ghi chú bổ sung",
        example = "Không có dấu hiệu bất thường khác"
    )
    String notes,

    @Schema(
        description = "ID người ghi nhận",
        example = "5"
    )
    Long recordedByUserId,

    @Schema(
        description = "Tên người ghi nhận",
        example = "Nguyễn Văn A"
    )
    String recordedByUserName,

    @Schema(
        description = "Thời gian tạo",
        example = "2024-06-01T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Thời gian cập nhật",
        example = "2024-06-01T10:15:00"
    )
    LocalDateTime updatedAt
) {}
