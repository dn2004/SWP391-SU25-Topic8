package com.fu.swp391.schoolhealthmanagementsystem.dto.supply;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Thông tin điều chỉnh tồn kho vật tư y tế")
public record MedicalSupplyStockAdjustmentDto(
        @Schema(
                description = "Số lượng cần điều chỉnh",
                example = "10"
        )
        @NotNull(message = "Số lượng không được để trống")
        @Min(
                value = 1,
                message = "Số lượng phải lớn hơn 0"
        )
        Integer quantity,

        @Schema(
                description = "Loại giao dịch",
                example = "Điều chỉnh giảm",
                allowableValues = {
                        "Điều chỉnh tăng",
                        "Điều chỉnh giảm"
                }
        )
        @NotNull(message = "Loại giao dịch không được để trống")
        SupplyTransactionType transactionType,

        @Schema(
                description = "Ghi chú về điều chỉnh",
                example = "Nhập thêm hàng"
        )
        @Size(
                max = 255,
                message = "Ghi chú không quá 255 ký tự"
        )
        String note
) {}
