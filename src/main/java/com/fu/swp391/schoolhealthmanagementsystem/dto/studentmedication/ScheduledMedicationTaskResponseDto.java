package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication; // Hoặc package phù hợp

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession; // Nếu bạn dùng enum này
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thông tin phản hồi nhiệm vụ uống thuốc đã lên lịch")
public record ScheduledMedicationTaskResponseDto(
        @Schema(description = "ID nhiệm vụ uống thuốc đã lên lịch")
        Long scheduledTaskId,
        @Schema(description = "ID đơn thuốc của học sinh")
        Long studentMedicationId,
        @Schema(description = "Tên thuốc")
        String medicationName,
        @Schema(description = "Ngày uống thuốc theo lịch")
        LocalDate scheduledDate,
        @Schema(description = "Thời gian uống thuốc theo lịch (dạng text)")
        String scheduledTimeText,
        @Schema(description = "Buổi học tương ứng với nhiệm vụ uống thuốc")
        SchoolSession schoolSession,
        @Schema(description = "Số liều cần uống trong nhiệm vụ")
        Integer dosesToAdminister,
        @Schema(description = "Trạng thái nhiệm vụ uống thuốc")
        ScheduledMedicationTaskStatus status,
        @Schema(description = "Thời điểm thực tế đã cho uống thuốc")
        LocalDateTime administeredAt,
        @Schema(description = "ID nhân viên y tế thực hiện")
        Long administeredByStaffId,
        @Schema(description = "Tên nhân viên y tế thực hiện")
        String administeredByStaffName,
        @Schema(description = "Ghi chú của nhân viên y tế")
        String staffNotes,
        @Schema(description = "Thời điểm tạo nhiệm vụ")
        LocalDateTime requestedAt
) {}