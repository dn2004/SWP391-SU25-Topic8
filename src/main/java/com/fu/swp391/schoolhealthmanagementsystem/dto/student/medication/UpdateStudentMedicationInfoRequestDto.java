package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Thông tin yêu cầu cập nhật thông tin đơn thuốc của học sinh")
public record UpdateStudentMedicationInfoRequestDto(
        @Schema(
                description = "Tên thuốc của học sinh",
                example = "Paracetamol"
        )
        @NotBlank(message = "Tên thuốc không được để trống")
        @Size(
                max = 200,
                message = "Tên thuốc không quá 200 ký tự"
        )
        String medicationName,

        @Schema(
                description = "Mô tả một liều dùng chuẩn",
                example = "1 viên 500mg"
        )
        @NotBlank(message = "Mô tả một liều dùng chuẩn không được để trống")
        @Size(
                max = 100,
                message = "Mô tả liều dùng không quá 100 ký tự"
        )
        String dosagePerAdministrationText,

        @Schema(
                description = "Ngày hết hạn của thuốc",
                example = "2025-12-31"
        )
        @FutureOrPresent(message = "Ngày hết hạn phải là hiện tại hoặc tương lai (nếu có)")
        LocalDate expiryDate,

        @Schema(
                description = "Ghi chú cho đơn thuốc",
                example = "Không dùng khi sốt cao trên 39 độ C"
        )
        @Size(
                max = 1000,
                message = "Ghi chú không quá 1000 ký tự"
        )
        String notes,

        @Schema(
                description = "Hướng dẫn sử dụng thuốc",
                example = "Uống sau khi ăn"
        )
        @Size(
                max = 1000,
                message = "Hướng dẫn sử dụng không quá 1000 ký tự"
        )
        String usageInstruction
){}
