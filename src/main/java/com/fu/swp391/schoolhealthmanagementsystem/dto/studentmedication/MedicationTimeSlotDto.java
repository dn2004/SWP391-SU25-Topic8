package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin cữ uống thuốc trong ngày")
public record MedicationTimeSlotDto(
        @NotBlank(message = "Thời điểm uống thuốc không được để trống")
        @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Thời điểm phải có định dạng HH:mm")
        @Schema(description = "Thời điểm cho uống thuốc, định dạng HH:mm", example = "08:00", requiredMode = Schema.RequiredMode.REQUIRED)
        String time,

        @Size(max = 200, message = "Ghi chú cho cữ uống không quá 200 ký tự")
        @Schema(description = "Ghi chú cụ thể cho cữ uống này (tùy chọn)", example = "Uống sau khi ăn no")
        String notes
) {}