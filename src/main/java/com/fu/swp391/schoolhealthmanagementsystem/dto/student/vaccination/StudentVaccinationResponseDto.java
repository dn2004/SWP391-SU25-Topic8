package com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO chứa thông tin chi tiết của một bản ghi tiêm chủng")
@Builder
public record StudentVaccinationResponseDto(
        @Schema(
                description = "ID của bản ghi tiêm chủng",
                example = "1"
        )
        Long studentVaccinationId,

        @Schema(
                description = "ID của học sinh liên quan",
                example = "101"
        )
        Long studentId,

        @Schema(
                description = "Tên đầy đủ của học sinh",
                example = "Nguyễn Văn An"
        )
        String studentFullName,

        @Schema(
                description = "Lớp của học sinh",
                example = "Mầm A"
        )
        String studentClassName,

        @Schema(
                description = "Tên vắc-xin",
                example = "Sởi - Quai bị - Rubella (MMR)"
        )
        String vaccineName,

        @Schema(
                description = "Ngày tiêm chủng",
                example = "2022-08-15"
        )
        LocalDate vaccinationDate,

        @Schema(
                description = "Nơi tiêm chủng",
                example = "Trung tâm Y tế Dự phòng Quận 1"
        )
        String provider,

        @Schema(
                description = "Ghi chú",
                example = "Phản ứng nhẹ sau tiêm"
        )
        String notes,

        @Schema(
                description = "Tên file bằng chứng gốc",
                example = "chung_nhan_tiem_chung.pdf"
        )
        String proofFileOriginalName,

        @Schema(
                description = "Loại file bằng chứng (MIME type)",
                example = "application/pdf"
        )
        String proofFileType,

        @Schema(
                description = "Cho biết bản ghi này có file bằng chứng đính kèm hay không"
        )
        boolean hasProofFile,

        @Schema(
                description = "Trạng thái hiện tại của bản ghi",
                example = "APPROVE"
        )
        StudentVaccinationStatus status,

        @Schema(
                description = "ID của người dùng đã duyệt (nếu có)",
                example = "5"
        )
        Long approvedByUserId,

        @Schema(
                description = "Ghi chú của người duyệt (nếu có)",
                example = "Đã xác minh."
        )
        String approverNotes,

        @Schema(
                description = "ID của người dùng đã tạo bản ghi",
                example = "2"
        )
        Long createdByUserId,

        @Schema(
                description = "Tên đầy đủ của người dùng đã tạo bản ghi",
                example = "Phụ Huynh Trần Văn A"
        )
        String createdByUserFullName,

        @Schema(
                description = "Tên đầy đủ của người dùng đã duyệt (nếu có)",
                example = "Y Tá Nguyễn"
        )
        String approvedByUserFullName,

        @Schema(
                description = "Thời điểm duyệt (nếu có)",
                example = "2023-10-29T14:30:00"
        )
        LocalDateTime approvedAt,

        @Schema(
                description = "ID của người dùng cập nhật lần cuối",
                example = "5"
        )
        Long updatedByUserId,

        @Schema(
                description = "Tên đầy đủ của người dùng cập nhật lần cuối",
                example = "Y Tá Nguyễn Thị B"
        )
        String updatedByUserFullName,

        @Schema(
                description = "Ngày tạo bản ghi",
                example = "2023-10-27T10:15:30"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "Ngày cập nhật bản ghi (nếu có)",
                example = "2023-10-28T11:00:00"
        )
        LocalDateTime updatedAt
) {
}