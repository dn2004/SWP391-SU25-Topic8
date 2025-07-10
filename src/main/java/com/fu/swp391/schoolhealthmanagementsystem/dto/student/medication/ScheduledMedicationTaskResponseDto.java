package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication; // Hoặc package phù hợp

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession; // Nếu bạn dùng enum này
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Thông tin phản hồi nhiệm vụ uống thuốc đã lên lịch")
public record ScheduledMedicationTaskResponseDto(
        @Schema(
                description = "ID nhiệm vụ uống thuốc đã lên lịch",
                example = "1"
        )
        Long scheduledTaskId,

        @Schema(
                description = "ID đơn thuốc của học sinh",
                example = "1"
        )
        Long studentMedicationId,

        @Schema(
                description = "Tên thuốc",
                example = "Paracetamol"
        )
        String medicationName,

        @Schema(
                description = "Ngày uống thuốc theo lịch",
                example = "2025-07-10"
        )
        LocalDate scheduledDate,

        @Schema(
                description = "Thời gian uống thuốc theo lịch (dạng text)",
                example = "08:00"
        )
        String scheduledTimeText,

        @Schema(
                description = "Buổi học tương ứng với nhiệm vụ uống thuốc",
                example = "Sáng"
        )
        SchoolSession schoolSession,

        @Schema(
                description = "Số liều cần uống trong nhiệm vụ",
                example = "1"
        )
        Integer dosesToAdminister,

        @Schema(
                description = "Trạng thái nhiệm vụ uống thuốc",
                example = "Đã cho uống"
        )
        ScheduledMedicationTaskStatus status,

        @Schema(
                description = "Thời điểm thực tế đã cho uống thuốc",
                example = "2025-07-10T08:05:00"
        )
        LocalDateTime administeredAt,

        @Schema(
                description = "ID nhân viên y tế thực hiện",
                example = "3"
        )
        Long administeredByStaffId,

        @Schema(
                description = "Tên nhân viên y tế thực hiện",
                example = "Nguyễn Thị C"
        )
        String administeredByStaffName,

        @Schema(
                description = "Ghi chú của nhân viên y tế",
                example = "Học sinh uống thuốc đúng giờ"
        )
        String staffNotes,

        @Schema(
                description = "Thời điểm tạo nhiệm vụ",
                example = "2025-07-09T07:00:00"
        )
        LocalDateTime requestedAt
) {}