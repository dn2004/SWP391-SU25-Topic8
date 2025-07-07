package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record VaccinationConsentResponseDto(
    @Schema(description = "ID của phiếu đồng ý")
    Long consentId,

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

    @Schema(description = "ID phụ huynh")
    Long parentId,

    @Schema(description = "Tên phụ huynh")
    String parentName,

    @Schema(description = "Trạng thái phiếu đồng ý")
    ConsentStatus status,

    @Schema(description = "Phản hồi của phụ huynh")
    String parentResponse,

    @Schema(description = "Ghi chú y tế")
    String medicalNotes,

    @Schema(description = "Thời gian gửi phiếu đồng ý")
    LocalDateTime consentFormSentAt,

    @Schema(description = "Thời gian nhận phản hồi")
    LocalDateTime responseReceivedAt,

    @Schema(description = "Thời gian gửi nhắc nhở")
    LocalDateTime reminderSentAt
) {}
