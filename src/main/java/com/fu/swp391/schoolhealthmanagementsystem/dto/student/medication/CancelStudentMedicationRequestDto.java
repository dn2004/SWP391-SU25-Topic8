package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin yêu cầu hủy thuốc cho học sinh")
public record CancelStudentMedicationRequestDto(
        @Schema(
                description = "Lý do hủy thuốc",
                example = "Nhập sai thông tin thuốc"
        )
        @NotBlank(message = "Lý do hủy thuốc không được để trống")
        @Size(
                max = 500,
                message = "Lý do hủy thuốc phải từ 10 đến 500 ký tự"
        )
        String cancellationReason
) {}
