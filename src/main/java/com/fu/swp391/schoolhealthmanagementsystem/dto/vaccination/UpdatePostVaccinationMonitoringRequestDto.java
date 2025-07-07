package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record UpdatePostVaccinationMonitoringRequestDto(
    @Schema(description = "Nhiệt độ cơ thể (°C)", example = "36.5")
    @DecimalMin(value = "30.0", message = "Nhiệt độ phải từ 30°C trở lên")
    @DecimalMax(value = "45.0", message = "Nhiệt độ không được vượt quá 45°C")
    Float temperature,

    @Schema(description = "Có phản ứng phụ hay không", example = "false")
    Boolean hasSideEffects,

    @Schema(description = "Mô tả phản ứng phụ", example = "Sưng nhẹ tại vị trí tiêm")
    @Size(max = 500, message = "Mô tả phản ứng phụ không quá 500 ký tự")
    String sideEffectsDescription,

    @Schema(description = "Các biện pháp đã thực hiện", example = "Đắp khăn lạnh, cho uống nước")
    @Size(max = 500, message = "Mô tả biện pháp không quá 500 ký tự")
    String actionsTaken,

    @Schema(description = "Ghi chú bổ sung", example = "Học sinh đã ổn định sau 30 phút")
    @Size(max = 1000, message = "Ghi chú không quá 1000 ký tự")
    String notes,

    @Schema(description = "Lý do cập nhật", example = "Cập nhật nhiệt độ sau khi đo lại")
    @Size(max = 200, message = "Lý do cập nhật không quá 200 ký tự")
    String reasonForUpdate
) {}
