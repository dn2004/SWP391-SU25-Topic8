package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Thông tin giao dịch đơn thuốc của học sinh trả về")
@Builder
public record StudentMedicationTransactionResponseDto(
        @Schema(description = "ID giao dịch đơn thuốc của học sinh")
        Long transactionId,
        @Schema(description = "Loại giao dịch đơn thuốc")
        StudentMedicationTransactionType transactionType,
        @Schema(description = "Số liều thay đổi trong giao dịch")
        Integer dosesChanged,
        @Schema(description = "Thời gian thực hiện giao dịch")
        LocalDateTime transactionDateTime,
        @Schema(description = "Người thực hiện giao dịch")
        String performedBy,
        @Schema(description = "ID nhiệm vụ uống thuốc liên quan (nếu có)")
        Long scheduledMedicationTaskId,
        @Schema(description = "Ghi chú cho giao dịch")
        String notes
) { }
