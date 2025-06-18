package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelStudentMedicationRequestDto(
    @Schema(description = "Lý do hủy thuốc", example = "Nhập sai thông tin thuốc")
    @NotBlank(message = "Lý do hủy thuốc không được để trống")
    @Size(min = 10, max = 500, message = "Lý do hủy thuốc phải từ 10 đến 500 ký tự")
    String cancellationReason
) {
}
