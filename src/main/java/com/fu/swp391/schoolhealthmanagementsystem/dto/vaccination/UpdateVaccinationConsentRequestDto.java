package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Yêu cầu cập nhật phiếu đồng ý tiêm chủng tại trường")
public record UpdateVaccinationConsentRequestDto(
    @Schema(
            description = "Trạng thái phiếu đồng ý (APPROVED/DECLINED)",
            example = "APPROVED"
    )
    @NotNull(message = "Trạng thái không được để trống")
    ConsentStatus status,

    @Schema(
            description = "Phản hồi của phụ huynh",
            example = "Con tôi không có tiền sử dị ứng thuốc"
    )
    @Size(
            max = 1000,
            message = "Phản hồi không quá 1000 ký tự"
    )
    String parentResponse
) {}
