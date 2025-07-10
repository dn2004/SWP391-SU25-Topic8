package com.fu.swp391.schoolhealthmanagementsystem.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HealthIncidentSupplyUsageDto(
        @Schema(
                description = "ID của vật tư",
                example = "1"
        )
        @NotNull(message = "ID vật tư không được để trống")
        Long supplyId,

        @Schema(
                description = "Số lượng sử dụng",
                example = "5"
        )
        @NotNull(message = "Số lượng sử dụng không được để trống")
        @Min(value = 1, message = "Số lượng sử dụng phải lớn hơn 0")
        Integer quantityUsed,

        @Schema(
                description = "Ghi chú sử dụng vật tư",
                example = "Dùng để băng bó vết thương"
        )
        @Size(max = 255, message = "Ghi chú sử dụng vật tư không quá 255 ký tự")
        String note
) {}
