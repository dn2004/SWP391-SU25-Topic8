package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;

import com.fu.swp391.schoolhealthmanagementsystem.validation.IsWorkday;
import com.fu.swp391.schoolhealthmanagementsystem.validation.StartDateBeforeExpiryDate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*; // Đảm bảo import đúng
import java.time.LocalDate;
import java.util.List;

@StartDateBeforeExpiryDate
@Schema(description = "Thông tin yêu cầu tạo đơn thuốc cho học sinh bởi nhân viên y tế")
public record CreateStudentMedicationByStaffRequestDto(
        @Schema(
                description = "ID Học sinh",
                example = "101"
        )
        @NotNull(message = "ID Học sinh không được để trống")
        Long studentId,

        @Schema(
                description = "Email Phụ huynh gửi",
                example = "parent@example.com"
        )
        @NotBlank(message = "Email Phụ huynh gửi không được để trống")
        @Email(message = "Email phụ huynh không đúng định dạng")
        String submittedByParentEmail,

        @Schema(
                description = "Tên thuốc",
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
                description = "Tổng số liều cung cấp",
                example = "10"
        )
        @NotNull(message = "Tổng số liều cung cấp không được để trống")
        @Min(value = 1, message = "Tổng số liều phải lớn hơn 0")
        Integer totalDosesProvided,

        @Schema(
                description = "Ngày hết hạn",
                example = "2025-12-31"
        )
        @FutureOrPresent(message = "Ngày hết hạn phải là hiện tại hoặc tương lai (nếu có)")
        LocalDate expiryDate,

        @Schema(
                description = "Ghi chú",
                example = "Không dùng khi sốt cao trên 39 độ C"
        )
        @Size(
                max = 1000,
                message = "Ghi chú không quá 1000 ký tự"
        )
        String notes,

        @Schema(
                description = "Hướng dẫn sử dụng",
                example = "Uống sau khi ăn"
        )
        @Size(
                max = 1000,
                message = "Hướng dẫn sử dụng không quá 1000 ký tự"
        )
        String usageInstruction,

        @Schema(
                description = "Ngày bắt đầu lịch trình",
                example = "2025-07-10"
        )
        @NotNull(message = "Ngày bắt đầu lịch trình không được để trống")
        @FutureOrPresent(message = "Ngày bắt đầu lịch trình phải là ngày hiện tại hoặc tương lai")
        @IsWorkday
        LocalDate scheduleStartDate,

        @Schema(description = "Danh sách các cữ uống trong ngày.")
        @NotNull(message = "Danh sách các cữ uống trong ngày không được để trống")
        @Size(
                min = 1,
                message = "Phải có ít nhất một cữ uống trong ngày"
        )
        @Valid
        List<MedicationTimeSlotDto> scheduleTimes
) {}