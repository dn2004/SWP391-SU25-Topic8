package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalSupplyUpdateDto(
        @Schema(description = "Tên vật tư y tế", example = "Khẩu trang y tế")
        @NotBlank(message = "Tên vật tư không được để trống")
        @Size(max = 100, message = "Tên vật tư không quá 100 ký tự")
        String name,

        @Schema(description = "Loại vật tư y tế", example = "Dụng cụ bảo hộ")
        @Size(max = 50, message = "Loại vật tư không quá 50 ký tự")
        String category,

        @Schema(description = "Đơn vị tính của vật tư", example = "Hộp")
        @Size(max = 20, message = "Đơn vị tính không quá 20 ký tự")
        String unit,

        @Schema(description = "Mô tả chi tiết về vật tư", example = "Khẩu trang y tế 3 lớp, màu xanh")
        @Size(max = 500, message = "Mô tả không quá 500 ký tự")
        String description
) {}