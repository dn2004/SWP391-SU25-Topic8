package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Phản hồi chiến dịch tiêm chủng tại trường")
public record VaccinationCampaignResponseDto(
    @Schema(
            description = "ID của chiến dịch tiêm chủng",
            example = "1"
    )
    Long campaignId,

    @Schema(
            description = "Tên chiến dịch tiêm chủng",
            example = "Tiêm chủng cúm mùa 2025"
    )
    String campaignName,

    @Schema(
            description = "Tên vắc-xin",
            example = "Vaxigrip Tetra"
    )
    String vaccineName,

    @Schema(
            description = "Mô tả chiến dịch",
            example = "Chiến dịch tiêm chủng cúm mùa cho học sinh toàn trường"
    )
    String description,

    @Schema(
            description = "Ngày tiêm chủng",
            example = "2025-10-15"
    )
    LocalDate vaccinationDate,

    @Schema(
            description = "Hạn chót gửi phiếu đồng ý",
            example = "2025-10-01"
    )
    LocalDate consentDeadline,

    @Schema(
            description = "Khối lớp học sinh được tiêm",
            example = "Lá"
    )
    ClassGroup targetClassGroup,

    @Schema(
            description = "Trạng thái chiến dịch",
            example = "SCHEDULED"
    )
    VaccinationCampaignStatus status,

    @Schema(
            description = "Ghi chú bổ sung",
            example = "Phụ huynh vui lòng điền đầy đủ thông tin"
    )
    String notes,

    @Schema(
            description = "ID người tạo chiến dịch",
            example = "2"
    )
    Long organizedByUserId,

    @Schema(
            description = "Tên người tạo chiến dịch",
            example = "Phòng y tế"
    )
    String organizedByUserName,

    @Schema(
            description = "Tên đơn vị y tế hỗ trợ",
            example = "Trung tâm y tế dự phòng ABC"
    )
    String healthcareProviderName,

    @Schema(
            description = "Thông tin liên hệ đơn vị y tế",
            example = "0123456789"
    )
    String healthcareProviderContact,

    @Schema(
            description = "Tổng số học sinh trong chiến dịch",
            example = "300"
    )
    Integer totalStudents,

    @Schema(
            description = "Số phiếu đồng ý đã nhận",
            example = "250"
    )
    Integer approvedConsents,

    @Schema(
            description = "Số phiếu từ chối đã nhận",
            example = "10"
    )
    Integer declinedConsents,

    @Schema(
            description = "Thời gian tạo",
            example = "2025-09-01T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
            description = "Thời gian cập nhật",
            example = "2025-09-01T11:00:00"
    )
    LocalDateTime updatedAt,

    @Schema(
            description = "ID người cập nhật",
            example = "2"
    )
    Long updatedByUserId,

    @Schema(
            description = "Tên người cập nhật",
            example = "Phòng y tế"
    )
    String updatedByUserName,

    @Schema(
            description = "Thời gian dời lịch",
            example = "2025-09-05T14:00:00"
    )
    LocalDateTime rescheduledAt,

    @Schema(
            description = "ID người dời lịch",
            example = "3"
    )
    Long rescheduledByUserId,

    @Schema(
            description = "Tên người dời lịch",
            example = "Ban giám hiệu"
    )
    String rescheduledByUserName
) {}
