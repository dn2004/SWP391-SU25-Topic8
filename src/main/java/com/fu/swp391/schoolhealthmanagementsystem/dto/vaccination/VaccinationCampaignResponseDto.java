package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VaccinationCampaignResponseDto(
    @Schema(description = "ID của chiến dịch tiêm chủng")
    Long campaignId,

    @Schema(description = "Tên chiến dịch tiêm chủng")
    String campaignName,

    @Schema(description = "Tên vắc-xin")
    String vaccineName,

    @Schema(description = "Mô tả chiến dịch")
    String description,

    @Schema(description = "Ngày tiêm chủng")
    LocalDate vaccinationDate,

    @Schema(description = "Hạn chót gửi phiếu đồng ý")
    LocalDate consentDeadline,

    @Schema(description = "Các khối lớp học sinh được tiêm", example = "MAM, CHOI, LA")
    ClassGroup targetClassGroup,

    @Schema(description = "Trạng thái chiến dịch")
    VaccinationCampaignStatus status,

    @Schema(description = "Ghi chú bổ sung")
    String notes,

    @Schema(description = "ID người tạo chiến dịch")
    Long organizedByUserId,

    @Schema(description = "Tên người tạo chiến dịch")
    String organizedByUserName,

    @Schema(description = "Tên đơn vị y tế hỗ trợ")
    String healthcareProviderName,

    @Schema(description = "Thông tin liên hệ đơn vị y tế")
    String healthcareProviderContact,

    @Schema(description = "Tổng số học sinh trong chiến dịch")
    Integer totalStudents,

    @Schema(description = "Số phiếu đồng ý đã nhận")
    Integer approvedConsents,

    @Schema(description = "Số phiếu từ chối đã nhận")
    Integer declinedConsents,

    @Schema(description = "Thời gian tạo")
    LocalDateTime createdAt,

    @Schema(description = "Thời gian cập nhật")
    LocalDateTime updatedAt,

    @Schema(description = "ID người cập nhật")
    Long updatedByUserId,

    @Schema(description = "Tên người cập nhật")
    String updatedByUserName,

    @Schema(description = "Thời gian dời lịch")
    LocalDateTime rescheduledAt,

    @Schema(description = "ID người dời lịch")
    Long rescheduledByUserId,

    @Schema(description = "Tên người dời lịch")
    String rescheduledByUserName
) {}
