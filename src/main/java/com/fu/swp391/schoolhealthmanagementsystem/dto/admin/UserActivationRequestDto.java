package com.fu.swp391.schoolhealthmanagementsystem.dto.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Yêu cầu (de)activate người dùng")
public class UserActivationRequestDto {
    @NotNull(message = "Trạng thái kích hoạt không được để trống")
    @Schema(description = "Đặt thành true để kích hoạt, false để vô hiệu hóa", example = "true")
    private Boolean isActive;
}