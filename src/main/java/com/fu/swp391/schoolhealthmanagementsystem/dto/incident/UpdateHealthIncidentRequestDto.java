package com.fu.swp391.schoolhealthmanagementsystem.dto.incident;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Yêu cầu cập nhật thông tin sự cố sức khỏe")
public record UpdateHealthIncidentRequestDto(
        @Schema(
                description = "Thời gian xảy ra sự cố",
                example = "2025-06-13T10:15:30"
        )
        @NotNull(message = "Thời gian xảy ra sự cố không được để trống")
        @PastOrPresent(message = "Thời gian xảy ra sự cố phải là quá khứ hoặc hiện tại")
        LocalDateTime incidentDateTime,

        @Schema(
                description = "Loại sự cố",
                example = "Chấn thương nhẹ"
        )
        @NotNull(message = "Loại sự cố không được để trống")
        HealthIncidentType incidentType,

        @Schema(
                description = "Mô tả sự cố",
                example = "Học sinh bị ngã trong giờ thể dục"
        )
        @NotBlank(message = "Mô tả sự cố không được để trống")
        @Size(
                min = 10,
                max = 2000,
                message = "Mô tả sự cố phải từ 10 đến 2000 ký tự"
        )
        String description,

        @Schema(
                description = "Hành động xử lý",
                example = "Đưa học sinh đến phòng y tế"
        )
        @NotBlank(message = "Hành động xử lý không được để trống")
        @Size(
                min = 10,
                max = 2000,
                message = "Hành động xử lý phải từ 10 đến 2000 ký tự"
        )
        String actionTaken,

        @Schema(
                description = "Địa điểm xảy ra sự cố",
                example = "Sân thể dục"
        )
        @Size(
                max = 100,
                message = "Địa điểm không quá 100 ký tự"
        )
        String location
) {}