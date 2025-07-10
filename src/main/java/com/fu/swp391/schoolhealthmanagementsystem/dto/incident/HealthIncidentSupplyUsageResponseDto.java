package com.fu.swp391.schoolhealthmanagementsystem.dto.incident;

import io.swagger.v3.oas.annotations.media.Schema;

public record HealthIncidentSupplyUsageResponseDto(
        @Schema(
                description = "ID của giao dịch",
                example = "1"
        )
        Long transactionId,

        @Schema(
                description = "ID của vật tư",
                example = "1"
        )
        Long supplyId,

        @Schema(
                description = "Tên của vật tư",
                example = "Băng gạc"
        )
        String supplyName,

        @Schema(
                description = "Số lượng sử dụng",
                example = "5"
        )
        Integer quantityUsed,

        @Schema(
                description = "Đơn vị tính",
                example = "Hộp"
        )
        String unit,

        @Schema(
                description = "Ghi chú sử dụng vật tư",
                example = "Dùng để băng bó vết thương"
        )
        String note
) {}