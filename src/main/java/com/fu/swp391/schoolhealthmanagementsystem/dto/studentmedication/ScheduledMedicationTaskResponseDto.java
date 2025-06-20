package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication; // Hoặc package phù hợp

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession; // Nếu bạn dùng enum này
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ScheduledMedicationTaskResponseDto(
        Long scheduledTaskId,
        Long studentMedicationId,
        String medicationName,
        LocalDate scheduledDate,
        String scheduledTimeText,
        SchoolSession schoolSession,
        Integer dosesToAdminister,
        ScheduledMedicationTaskStatus status,
        LocalDateTime administeredAt,
        Long administeredByStaffId,
        String administeredByStaffName,
        String staffNotes,
        LocalDateTime requestedAt
) {}