package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thông tin vật tư y tế trả về")
public record MedicalSupplyResponseDto(
        @Schema(
                description = "ID của vật tư y tế",
                example = "1"
        )
        Long supplyId,

        @Schema(
                description = "Tên vật tư y tế",
                example = "Khẩu trang y tế"
        )
        String name,

        @Schema(
                description = "Loại vật tư y tế",
                example = "Dụng cụ bảo hộ"
        )
        String category,

        @Schema(
                description = "Đơn vị tính của vật tư",
                example = "Hộp"
        )
        String unit,

        @Schema(
                description = "Số lượng tồn kho hiện tại",
                example = "50"
        )
        Integer currentStock,

        @Schema(
                description = "Mô tả chi tiết về vật tư",
                example = "Khẩu trang y tế 3 lớp, màu xanh"
        )
        String description,

        @Schema(
                description = "Ngày hết hạn của vật tư",
                example = "2026-01-15")
        LocalDate expiredDate,

        @Schema(
                description = "Ngày tạo vật tư",
                example = "2025-06-13T10:15:30"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Ngày cập nhật cuối cùng",
                example = "2025-06-14T12:00:00"
        )
        LocalDateTime lastUpdatedAt,

        @Schema(
                description = "Email của người tạo",
                example = "admin@example.com"
        )
        String createdByUserEmail,

        @Schema(
                description = "Email của người cập nhật",
                example = "manager@example.com"
        )
        String updatedByUserEmail,

        @Schema(
                description = "Trạng thái của vật tư y tế",
                example = "Hết hàng"
        )
        MedicalSupplyStatus status
) {}