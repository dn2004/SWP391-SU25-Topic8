 package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.validation.IsWorkday;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;


// DTO để NVYT cập nhật/thiết lập thông tin lịch trình cho StudentMedication
@Slf4j
public record UpdateMedicationScheduleRequestDto(
        @NotNull(message = "Ngày bắt đầu lịch trình không được để trống")
        @IsWorkday
        @FutureOrPresent
        LocalDate scheduleStartDate,

        @NotNull(message = "Danh sách các cữ uống trong ngày không được để trống")
        @Size(min = 1, message = "Phải có ít nhất một cữ uống trong ngày")
        @Valid
        @Schema(description = "Danh sách các cữ uống trong ngày. Mỗi cữ uống bao gồm thời gian, số liều và ghi chú (nếu có).")
        List<MedicationTimeSlotDto> scheduleTimes
) {}