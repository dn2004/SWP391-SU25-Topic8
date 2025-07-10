package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Thông tin giao dịch đơn thuốc của học sinh trả về")
@Builder
public record StudentMedicationTransactionResponseDto(
        @Schema(
                description = "ID giao dịch đơn thuốc của học sinh",
                example = "1"
        )
        Long transactionId,

        @Schema(
                description = "Loại giao dịch đơn thuốc",
                example = "Đã cho uống"
        )
        StudentMedicationTransactionType transactionType,

        @Schema(
                description = "Số liều thay đổi trong giao dịch",
                example = "1"
        )
        Integer dosesChanged,

        @Schema(
                description = "Thời gian thực hiện giao dịch",
                example = "2025-07-09T09:00:00"
        )
        LocalDateTime transactionDateTime,

        @Schema(
                description = "Người thực hiện giao dịch",
                example = "Nguyễn Thị C"
        )
        String performedBy,

        @Schema(
                description = "ID nhiệm vụ uống thuốc liên quan (nếu có)",
                example = "1"
        )
        Long scheduledMedicationTaskId,

        @Schema(
                description = "Ghi chú cho giao dịch",
                example = "Đã cho uống thuốc vào tiết 2"
        )
        String notes
) {}
