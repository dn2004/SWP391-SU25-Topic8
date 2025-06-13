package com.fu.swp391.schoolhealthmanagementsystem.dto.incident;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record HealthIncidentResponseDto(
        @Schema(description = "ID của sự cố sức khỏe", example = "1")
        Long incidentId,

        @Schema(description = "ID của học sinh", example = "101")
        Long studentId,

        @Schema(description = "Tên của học sinh", example = "Nguyen Van A")
        String studentName,

        @Schema(description = "Lớp của học sinh", example = "10A1")
        String studentClass,

        @Schema(description = "ID của người ghi nhận", example = "201")
        Long recordedByUserId,

        @Schema(description = "Tên của người ghi nhận", example = "Dr. Tran Van B")
        String recordedByUserName,

        @Schema(description = "Thời gian xảy ra sự cố", example = "2025-06-13T10:15:30")
        LocalDateTime incidentDateTime,

        @Schema(description = "Loại sự cố", example = "INJURY")
        HealthIncidentType incidentType,

        @Schema(description = "Mô tả sự cố", example = "Học sinh bị ngã trong giờ thể dục")
        String description,

        @Schema(description = "Hành động xử lý", example = "Đưa học sinh đến phòng y tế")
        String actionTaken,

        @Schema(description = "Địa điểm xảy ra sự cố", example = "Sân thể dục")
        String location,

        @Schema(description = "Thời gian tạo sự cố", example = "2025-06-13T10:20:00")
        LocalDateTime createdAt,

        @Schema(description = "Thời gian cập nhật cuối cùng", example = "2025-06-13T11:00:00")
        LocalDateTime updatedAt,

        @Schema(description = "ID của người cập nhật cuối cùng", example = "202")
        Long updatedByUserId,

        @Schema(description = "Tên của người cập nhật cuối cùng", example = "Dr. Le Van C")
        String updatedByUserName,

        @Schema(description = "Danh sách sử dụng vật tư y tế")
        List<HealthIncidentSupplyUsageResponseDto> supplyUsages
) {}
