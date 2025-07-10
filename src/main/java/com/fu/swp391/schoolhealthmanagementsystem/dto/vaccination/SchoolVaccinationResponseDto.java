package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Phản hồi tiêm chủng tại trường")
public record SchoolVaccinationResponseDto(
    @Schema(
            description = "ID tiêm chủng tại trường",
            example = "1"
    )
    Long schoolVaccinationId,

    @Schema(
            description = "ID chiến dịch tiêm chủng",
            example = "1"
    )
    Long campaignId,

    @Schema(
            description = "Tên chiến dịch tiêm chủng",
            example = "Tiêm chủng cúm mùa 2025"
    )
    String campaignName,

    @Schema(
            description = "ID học sinh",
            example = "101"
    )
    Long studentId,

    @Schema(
            description = "Tên học sinh",
            example = "Nguyễn Văn An"
    )
    String studentName,

    @Schema(
            description = "Lớp của học sinh",
            example = "Lá 1"
    )
    String studentClass,

    @Schema(
            description = "ID phiếu đồng ý",
            example = "201"
    )
    Long consentId,

    @Schema(
            description = "Trạng thái tiêm chủng",
            example = "Đã hoàn thành"
    )
    SchoolVaccinationStatus status,

    @Schema(
            description = "Ngày tiêm chủng",
            example = "2025-07-20"
    )
    LocalDate vaccinationDate,

    @Schema(
            description = "Ghi chú",
            example = "Học sinh khỏe mạnh, không có biểu hiện bất thường."
    )
    String notes,

    @Schema(
            description = "ID người thực hiện tiêm",
            example = "5"
    )
    Long administeredByUserId,

    @Schema(
            description = "Tên người thực hiện tiêm",
            example = "Y tá Trần Thị B"
    )
    String administeredByUserName,

    @Schema(description = "Bản ghi theo dõi sau tiêm")
    PostVaccinationMonitoringResponseDto monitoringRecord,

    @Schema(
            description = "Thời gian tạo",
            example = "2025-07-20T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
            description = "Thời gian cập nhật",
            example = "2025-07-20T10:00:00"
    )
    LocalDateTime updatedAt
) {}
