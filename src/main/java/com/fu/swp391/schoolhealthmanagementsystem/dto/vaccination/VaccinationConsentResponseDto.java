package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Phản hồi phiếu đồng ý tiêm chủng tại trường")
public record VaccinationConsentResponseDto(
    @Schema(
            description = "ID của phiếu đồng ý",
            example = "1"
    )
    Long consentId,

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
            description = "ID phụ huynh",
            example = "201"
    )
    Long parentId,

    @Schema(
            description = "Tên phụ huynh",
            example = "Nguyễn Thị B"
    )
    String parentName,

    @Schema(
            description = "Trạng thái phiếu đồng ý",
            example = "APPROVED"
    )
    ConsentStatus status,

    @Schema(
            description = "Phản hồi của phụ huynh",
            example = "Đồng ý cho con tiêm"
    )
    String parentResponse,

    @Schema(
            description = "Ghi chú y tế",
            example = "Không có tiền sử dị ứng"
    )
    String medicalNotes,

    @Schema(
            description = "Thời gian gửi phiếu đồng ý",
            example = "2025-09-01T10:00:00"
    )
    LocalDateTime consentFormSentAt,

    @Schema(
            description = "Thời gian nhận phản hồi",
            example = "2025-09-05T14:30:00"
    )
    LocalDateTime responseReceivedAt,

    @Schema(
            description = "Thời gian gửi nhắc nhở",
            example = "2025-09-10T09:00:00"
    )
    LocalDateTime reminderSentAt
) {}
