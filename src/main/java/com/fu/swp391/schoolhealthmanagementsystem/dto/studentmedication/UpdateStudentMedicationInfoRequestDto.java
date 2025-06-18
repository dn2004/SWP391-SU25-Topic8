package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateStudentMedicationInfoRequestDto(
        @NotBlank(message = "Tên thuốc không được để trống")
        @Size(max = 200, message = "Tên thuốc không quá 200 ký tự")
        String medicationName,

        @NotBlank(message = "Mô tả một liều dùng chuẩn không được để trống")
        @Size(max = 100, message = "Mô tả liều dùng không quá 100 ký tự")
        String dosagePerAdministrationText,

        @FutureOrPresent(message = "Ngày hết hạn phải là hiện tại hoặc tương lai (nếu có)")
        LocalDate expiryDate,

        @Size(max = 1000, message = "Ghi chú không quá 1000 ký tự")
        String notes,

        @Size(max = 1000, message = "Hướng dẫn sử dụng không quá 1000 ký tự")
        String usageInstruction)
{}
