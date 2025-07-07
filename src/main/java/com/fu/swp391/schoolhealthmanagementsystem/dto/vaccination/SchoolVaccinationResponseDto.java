package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SchoolVaccinationResponseDto(
    @Schema(description = "ID tiêm chủng tại trường")
    Long schoolVaccinationId,

    @Schema(description = "ID chiến dịch tiêm chủng")
    Long campaignId,

    @Schema(description = "Tên chiến dịch tiêm chủng")
    String campaignName,

    @Schema(description = "ID học sinh")
    Long studentId,

    @Schema(description = "Tên học sinh")
    String studentName,

    @Schema(description = "Lớp của học sinh")
    String studentClass,

    @Schema(description = "ID phiếu đồng ý")
    Long consentId,

    @Schema(description = "Trạng thái tiêm chủng")
    SchoolVaccinationStatus status,

    @Schema(description = "Ngày tiêm chủng")
    LocalDate vaccinationDate,

    @Schema(description = "Ghi chú")
    String notes,

    @Schema(description = "ID người thực hiện tiêm")
    Long administeredByUserId,

    @Schema(description = "Tên người thực hiện tiêm")
    String administeredByUserName,

    @Schema(description = "Bản ghi theo dõi sau tiêm")
    PostVaccinationMonitoringResponseDto monitoringRecord,

    @Schema(description = "Thời gian tạo")
    LocalDateTime createdAt,

    @Schema(description = "Thời gian cập nhật")
    LocalDateTime updatedAt
) {}
