package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidAgeRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Yêu cầu tạo chiến dịch tiêm chủng tại trường")
@ValidAgeRange
public record CreateVaccinationCampaignRequestDto(
    @Schema(
            description = "Tên chiến dịch tiêm chủng",
            example = "Tiêm chủng vắc-xin Covid-19 đợt 1"
    )
    @NotBlank(message = "Tên chiến dịch không được để trống")
    @Size(
            max = 200,
            message = "Tên chiến dịch phải không được để trống"
    )
    String campaignName,

    @Schema(
            description = "Tên vắc-xin",
            example = "Vắc-xin Covid-19 Pfizer"
    )
    @NotBlank(message = "Tên vắc-xin không được để trống")
    String vaccineName,

    @Schema(
            description = "Mô tả chiến dịch",
            example = "Chiến dịch tiêm chủng Covid-19 cho học sinh toàn trường"
    )
    @Size(
            max = 1000,
            message = "Mô tả không quá 1000 ký tự"
    )
    String description,

    @Schema(
            description = "Ngày tiêm chủng",
            example = "2025-07-15"
    )
    @NotNull(message = "Ngày tiêm chủng không được để trống")
    @FutureOrPresent(message = "Ngày tiêm chủng phải từ hôm nay trở đi")
    LocalDate vaccinationDate,

//    @Schema(
//            description = "Hạn chót gửi phiếu đồng ý",
//            example = "2025-07-10"
//    )
//    @NotNull(message = "Hạn chót gửi phiếu đồng ý không được để trống")
//    @FutureOrPresent(message = "Hạn chót gửi phiếu đồng ý phải từ hôm nay trở đi")
//    LocalDate consentDeadline,

    @Schema(
            description = "Các khối lớp học sinh được tiêm",
            example = "Mầm"
    )
    ClassGroup targetClassGroup,

    @Schema(
            description = "Tuổi tối thiểu",
            example = "12"
    )
    @Size(min = 0)
    Integer targetAgeMin,


    @Schema(
            description = "Tuổi tối đa",
            example = "18"
    )
    @Size(min = 0)
    Integer targetAgeMax,

    @Schema(
            description = "Ghi chú bổ sung",
            example = "Học sinh cần mang theo phiếu tiêm chủng cá nhân"
    )
    @Size(
            max = 1000,
            message = "Ghi chú không quá 1000 ký tự"
    )
    String notes,

    @Schema(
            description = "Tên đơn vị y tế hỗ trợ",
            example = "Trung tâm Y tế Quận 1"
    )
    @Size(
            max = 200,
            message = "Tên đơn vị y tế không quá 200 ký tự"
    )
    String healthcareProviderName,

    @Schema(
            description = "Thông tin liên hệ đơn vị y tế",
            example = "0914334556"
    )
    @Size(
            max = 100,
            message = "Thông tin liên hệ không quá 100 ký tự"
    )
    String healthcareProviderContact
) {}
