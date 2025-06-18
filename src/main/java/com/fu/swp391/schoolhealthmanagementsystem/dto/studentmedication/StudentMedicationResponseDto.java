package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record StudentMedicationResponseDto(
        Long studentMedicationId,
        Long studentId,
        String studentFullName,
        Long submittedByParentId,
        String parentFullName,
        String medicationName,
        String dosagePerAdministrationText,
        Integer totalDosesProvided,
        Integer remainingDoses,
        LocalDate expiryDate,
        LocalDate dateReceived,
        Long receivedByMedicalStaffId,
        String medicalStaffFullName,
        MedicationStatus status,
        String notes,
        String usageInstruction,
        String rejectionReason,
        LocalDateTime createdAt,
        Long createdByUserId,
        String createdByUserName,
        LocalDateTime updatedAt,
        Long updatedByUserId,
        String updatedByUserName,
        LocalDate scheduleStartDate,
        // Đổi từ String scheduleTimesJson sang List<MedicationTimeSlotDto>
        @Schema(description = "Danh sách chi tiết các cữ uống trong ngày")
        List<MedicationTimeSlotDto> scheduleTimes
) {}