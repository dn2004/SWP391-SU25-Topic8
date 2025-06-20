package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record StudentMedicationTransactionResponseDto(
        Long transactionId,
        StudentMedicationTransactionType transactionType,
        Integer dosesChanged,
        LocalDateTime transactionDateTime,
        String performedBy,
        Long scheduledMedicationTaskId,
        String notes
) { }

