package com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO chứa thông tin chi tiết của một bệnh mãn tính của học sinh")
@Builder
public record StudentChronicDiseaseResponseDto(
        @Schema(
                description = "ID của bản ghi bệnh mãn tính",
                example = "1"
        )
        Long id,

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
                example = "Mầm Chồi 1"
        )
        String studentClassName,

        @Schema(
                description = "Tên bệnh mãn tính",
                example = "Hen suyễn"
        )
        String diseaseName,

        @Schema(
                description = "Ngày được chẩn đoán",
                example = "2020-05-10"
        )
        LocalDate diagnosedDate,

        @Schema(
                description = "Bác sĩ chẩn đoán",
                example = "BS. Nguyễn Thị B"
        )
        String diagnosingDoctor,

        @Schema(
                description = "Ghi chú chi tiết",
                example = "Cần mang theo ống hít Ventolin."
        )
        String notes,

        @Schema(
                description = "Tên file đính kèm gốc",
                example = "giay_kham_benh.pdf"
        )
        String attachmentFileOriginalName,

        @Schema(
                description = "Loại file đính kèm (MIME type)",
                example = "application/pdf"
        )
        String attachmentFileType,

        @Schema(description = "Cho biết bản ghi này có file đính kèm hay không")
        boolean hasAttachmentFile,

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
