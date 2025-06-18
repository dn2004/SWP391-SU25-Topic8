package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.validation.IsWorkday;
import com.fu.swp391.schoolhealthmanagementsystem.validation.StartDateBeforeExpiryDate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*; // Đảm bảo import đúng
import java.time.LocalDate;
import java.util.List;

@StartDateBeforeExpiryDate
public record CreateStudentMedicationByStaffRequestDto(
        @NotNull(message = "ID Học sinh không được để trống")
        Long studentId,

        @NotBlank(message = "Email Phụ huynh gửi không được để trống")
        @Email(message = "Email phụ huynh không đúng định dạng")
        String submittedByParentEmail,

        @NotBlank(message = "Tên thuốc không được để trống")
        @Size(max = 200, message = "Tên thuốc không quá 200 ký tự")
        String medicationName,

        @NotBlank(message = "Mô tả một liều dùng chuẩn không được để trống")
        @Size(max = 100, message = "Mô tả liều dùng không quá 100 ký tự")
        String dosagePerAdministrationText,

        @NotNull(message = "Tổng số liều cung cấp không được để trống")
        @Min(value = 1, message = "Tổng số liều phải lớn hơn 0")
        Integer totalDosesProvided,

        @FutureOrPresent(message = "Ngày hết hạn phải là hiện tại hoặc tương lai (nếu có)")
        LocalDate expiryDate,

        @Size(max = 1000, message = "Ghi chú không quá 1000 ký tự")
        String notes,

        @Size(max = 1000, message = "Hướng dẫn sử dụng không quá 1000 ký tự")
        String usageInstruction,

        @NotNull(message = "Ngày bắt đầu lịch trình không được để trống")
        @FutureOrPresent(message = "Ngày bắt đầu lịch trình phải là ngày hiện tại hoặc tương lai")
        @IsWorkday
        LocalDate scheduleStartDate,


        @NotNull(message = "Danh sách các cữ uống trong ngày không được để trống")
        @Size(min = 1, message = "Phải có ít nhất một cữ uống trong ngày")
        @Valid
        @Schema(description = "Danh sách các cữ uống trong ngày.")
        List<MedicationTimeSlotDto> scheduleTimes
) {}