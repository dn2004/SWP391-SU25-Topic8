package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

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

