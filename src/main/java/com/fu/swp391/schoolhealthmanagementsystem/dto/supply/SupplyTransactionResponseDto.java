package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Thông tin giao dịch vật tư y tế trả về")
@Builder
public record SupplyTransactionResponseDto(
        Long transactionId,
        Long supplyId,
        String supplyName,
        Integer quantity,
        SupplyTransactionType supplyTransactionType,
        Long incidentId,
        String note,
        LocalDateTime transactionDateTime,
        String performedBy
) { }
