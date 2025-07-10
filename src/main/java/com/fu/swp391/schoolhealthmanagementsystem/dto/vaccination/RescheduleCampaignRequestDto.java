package com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "Yêu cầu dời lịch tiêm chủng tại trường")
public record RescheduleCampaignRequestDto(
        @Schema(
                description = "Ngày tiêm chủng mới",
                example = "2025-08-15"
        )
        @NotNull(message = "Ngày tiêm chủng mới không được để trống")
        @FutureOrPresent(message = "Ngày tiêm chủng mới phải từ hôm nay trở đi")
        LocalDate newVaccinationDate,

        @Schema(
                description = "Lý do dời lịch",
                example = "Do điều kiện thời tiết không thuận lợi"
        )
        @NotBlank(message = "Lý do dời lịch không được để trống")
        String reason
) {
}
