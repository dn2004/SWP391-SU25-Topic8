package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Thông tin đơn thuốc của học sinh trả về")
public record StudentMedicationResponseDto(
        @Schema(
                description = "ID đơn thuốc của học sinh",
                example = "1"
        )
        Long studentMedicationId,

        @Schema(
                description = "ID học sinh",
                example = "1"
        )
        Long studentId,

        @Schema(
                description = "Họ tên học sinh",
                example = "Nguyễn Văn A"
        )
        String studentFullName,

        @Schema(
                description = "ID phụ huynh gửi đơn",
                example = "2"
        )
        Long submittedByParentId,

        @Schema(
                description = "Họ tên phụ huynh gửi đơn",
                example = "Trần Văn B"
        )
        String parentFullName,

        @Schema(
                description = "Tên thuốc",
                example = "Paracetamol"
        )
        String medicationName,

        @Schema(
                description = "Mô tả một liều dùng chuẩn",
                example = "1 viên 500mg"
        )
        String dosagePerAdministrationText,

        @Schema(
                description = "Tổng số liều cung cấp",
                example = "10"
        )
        Integer totalDosesProvided,

        @Schema(
                description = "Số liều còn lại",
                example = "5"
        )
        Integer remainingDoses,

        @Schema(
                description = "Ngày hết hạn của thuốc",
                example = "2025-12-31"
        )
        LocalDate expiryDate,

        @Schema(
                description = "Ngày nhận thuốc",
                example = "2025-07-01"
        )
        LocalDate dateReceived,

        @Schema(
                description = "ID nhân viên y tế nhận thuốc",
                example = "301"
        )
        Long receivedByMedicalStaffId,

        @Schema(
                description = "Họ tên nhân viên y tế nhận thuốc",
                example = "Nguyễn Thị C"
        )
        String medicalStaffFullName,

        @Schema(
                description = "Trạng thái đơn thuốc",
                example = "Sẵn có"
        )
        MedicationStatus status,

        @Schema(
                description = "Ghi chú cho đơn thuốc",
                example = "Không dùng khi sốt cao trên 39 độ C"
        )
        String notes,

        @Schema(
                description = "Hướng dẫn sử dụng thuốc",
                example = "Uống sau khi ăn"
        )
        String usageInstruction,

        @Schema(
                description = "Thời điểm tạo đơn thuốc",
                example = "2025-07-01T08:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "ID người tạo đơn thuốc",
                example = "201"
        )
        Long createdByUserId,

        @Schema(
                description = "Tên người tạo đơn thuốc",
                example = "Trần Văn B"
        )
        String createdByUserName,

        @Schema(
                description = "Thời điểm cập nhật đơn thuốc",
                example = "2025-07-09T10:00:00"
        )
        LocalDateTime updatedAt,

        @Schema(
                description = "ID người cập nhật đơn thuốc",
                example = "301"
        )
        Long updatedByUserId,

        @Schema(
                description = "Tên người cập nhật đơn thuốc",
                example = "Nguyễn Thị C"
        )
        String updatedByUserName,

        @Schema(
                description = "Ngày bắt đầu lịch trình uống thuốc",
                example = "2025-07-10"
        )
        LocalDate scheduleStartDate,

        @Schema(description = "Danh sách chi tiết các cữ uống trong ngày")
        List<MedicationTimeSlotDto> scheduleTimes
) {}