package com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Thông tin đơn thuốc của học sinh trả về")
public record StudentMedicationResponseDto(
        @Schema(description = "ID đơn thuốc của học sinh")
        Long studentMedicationId,
        @Schema(description = "ID học sinh")
        Long studentId,
        @Schema(description = "Họ tên học sinh")
        String studentFullName,
        @Schema(description = "ID phụ huynh gửi đơn")
        Long submittedByParentId,
        @Schema(description = "Họ tên phụ huynh gửi đơn")
        String parentFullName,
        @Schema(description = "Tên thuốc")
        String medicationName,
        @Schema(description = "Mô tả một liều dùng chuẩn")
        String dosagePerAdministrationText,
        @Schema(description = "Tổng số liều cung cấp")
        Integer totalDosesProvided,
        @Schema(description = "Số liều còn lại")
        Integer remainingDoses,
        @Schema(description = "Ngày hết hạn của thuốc")
        LocalDate expiryDate,
        @Schema(description = "Ngày nhận thuốc")
        LocalDate dateReceived,
        @Schema(description = "ID nhân viên y tế nhận thuốc")
        Long receivedByMedicalStaffId,
        @Schema(description = "Họ tên nhân viên y tế nhận thuốc")
        String medicalStaffFullName,
        @Schema(description = "Trạng thái đơn thuốc")
        MedicationStatus status,
        @Schema(description = "Ghi chú cho đơn thuốc")
        String notes,
        @Schema(description = "Hướng dẫn sử dụng thuốc")
        String usageInstruction,
        @Schema(description = "Thời điểm tạo đơn thuốc")
        LocalDateTime createdAt,
        @Schema(description = "ID người tạo đơn thuốc")
        Long createdByUserId,
        @Schema(description = "Tên người tạo đơn thuốc")
        String createdByUserName,
        @Schema(description = "Thời điểm cập nhật đơn thuốc")
        LocalDateTime updatedAt,
        @Schema(description = "ID người cập nhật đơn thuốc")
        Long updatedByUserId,
        @Schema(description = "Tên người cập nhật đơn thuốc")
        String updatedByUserName,
        @Schema(description = "Ngày bắt đầu lịch trình uống thuốc")
        LocalDate scheduleStartDate,
        // Đổi từ String scheduleTimesJson sang List<MedicationTimeSlotDto>
        @Schema(description = "Danh sách chi tiết các cữ uống trong ngày")
        List<MedicationTimeSlotDto> scheduleTimes
) {}