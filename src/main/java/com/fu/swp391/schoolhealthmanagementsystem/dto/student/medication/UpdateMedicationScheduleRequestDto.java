package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;
import com.fu.swp391.schoolhealthmanagementsystem.validation.IsWorkday;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Schema(description = "Thông tin yêu cầu cập nhật lịch trình uống thuốc cho học sinh")
public record UpdateMedicationScheduleRequestDto(
        @Schema(
                description = "Ngày bắt đầu lịch trình",
                example = "2025-07-10"
        )
        @NotNull(message = "Ngày bắt đầu lịch trình không được để trống")
        @IsWorkday
        @FutureOrPresent
        LocalDate scheduleStartDate,

        @Schema(description = "Danh sách các cữ uống trong ngày. Mỗi cữ uống bao gồm thời gian, số liều và ghi chú (nếu có).")
        @NotNull(
                message = "Danh sách các cữ uống trong ngày không được để trống"
        )
        @Size(min = 1, message = "Phải có ít nhất một cữ uống trong ngày")
        @Valid
        List<MedicationTimeSlotDto> scheduleTimes
) {}