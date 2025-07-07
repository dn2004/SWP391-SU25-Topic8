package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PostVaccinationMonitoringResponseDto(
    @Schema(description = "ID theo dõi sau tiêm")
    Long monitoringId,

    @Schema(description = "ID tiêm chủng tại trường")
    Long schoolVaccinationId,

    @Schema(description = "Thời gian theo dõi")
    LocalDateTime monitoringTime,

    @Schema(description = "Nhiệt độ (°C)")
    Float temperature,

    @Schema(description = "Có phản ứng phụ hay không")
    Boolean hasSideEffects,

    @Schema(description = "Mô tả phản ứng phụ")
    String sideEffectsDescription,

    @Schema(description = "Hành động đã thực hiện")
    String actionsTaken,

    @Schema(description = "Ghi chú bổ sung")
    String notes,

    @Schema(description = "ID người ghi nhận")
    Long recordedByUserId,

    @Schema(description = "Tên người ghi nhận")
    String recordedByUserName,

    @Schema(description = "Thời gian tạo")
    LocalDateTime createdAt,

    @Schema(description = "Thời gian cập nhật")
    LocalDateTime updatedAt
) {}
