package com.fu.swp391.schoolhealthmanagementsystem.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Yêu cầu (de)activate người dùng")
public record UserActivationRequestDto(
        @NotNull(message = "Trạng thái kích hoạt là bắt buộc")
        @Schema(
                description = "Đặt thành true để kích hoạt, false để vô hiệu hóa",
                example = "true",
                allowableValues = {"true", "false"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Boolean isActive
) {
}
