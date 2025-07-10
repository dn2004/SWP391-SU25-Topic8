package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Thông tin giao dịch vật tư y tế trả về")
@Builder
public record SupplyTransactionResponseDto(
        @Schema(
                description = "ID giao dịch",
                example = "1"
        )
        Long transactionId,

        @Schema(
                description = "ID vật tư",
                example = "1"
        )
        Long supplyId,

        @Schema(
                description = "Tên vật tư",
                example = "Khẩu trang y tế"
        )
        String supplyName,

        @Schema(
                description = "Số lượng vật tư",
                example = "50"
        )
        Integer quantity,

        @Schema(
                description = "Loại giao dịch vật tư",
                example = "Điều chỉnh tăng"
        )
        SupplyTransactionType supplyTransactionType,

        @Schema(
                description = "ID sự cố liên quan",
                example = "1"
        )
        Long incidentId,

        @Schema(
                description = "Ghi chú giao dịch",
                example = "Nhập kho bổ sung"
        )
        String note,

        @Schema(
                description = "Thời gian giao dịch",
                example = "2024-06-20T10:30:00"
        )
        LocalDateTime transactionDateTime,

        @Schema(
                description = "Người thực hiện giao dịch",
                example = "Nguyễn Văn A"
        )
        String performedBy
) { }
