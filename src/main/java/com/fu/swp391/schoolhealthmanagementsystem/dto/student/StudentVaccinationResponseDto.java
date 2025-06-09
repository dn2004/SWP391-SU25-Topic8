package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder; // Sử dụng Builder để dễ tạo đối tượng
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "DTO chứa thông tin chi tiết của một bản ghi tiêm chủng")
@Builder // Lombok Builder
public record StudentVaccinationResponseDto(
        @Schema(description = "ID của bản ghi tiêm chủng", example = "1")
        Long studentVaccinationId,

        @Schema(description = "ID của học sinh liên quan", example = "101")
        Long studentId, // Chỉ trả về ID của học sinh, không phải toàn bộ đối tượng Student

        @Schema(description = "Tên vắc-xin", example = "Sởi - Quai bị - Rubella (MMR)")
        String vaccineName,

        @Schema(description = "Ngày tiêm chủng", example = "2022-08-15")
        LocalDate vaccinationDate,

        // Thêm các trường từ Entity StudentVaccination mà bạn muốn trả về
        // Ví dụ: provider, notes nếu có và cần thiết
        @Schema(description = "Nơi tiêm chủng", example = "Trung tâm Y tế Dự phòng Quận 1")
        String provider, // Giả sử Entity có trường này

        @Schema(description = "Ghi chú", example = "Phản ứng nhẹ sau tiêm")
        String notes, // Giả sử Entity có trường này

        @Schema(description = "Tên file bằng chứng gốc", example = "chung_nhan_tiem_chung.pdf")
        String proofFileOriginalName,

        @Schema(description = "URL để xem/tải file bằng chứng", example = "https://res.cloudinary.com/.../chung_nhan.pdf")
        String proofFileUrl,

        @Schema(description = "Loại file bằng chứng (MIME type)", example = "application/pdf")
        String proofFileType,

        @Schema(description = "Trạng thái hiện tại", example = "Chờ xử lý")
        StudentVaccinationStatus status,


        @Schema(description = "Ngày tạo bản ghi", example = "2023-10-27T10:15:30")
        LocalDateTime createdAt,

        @Schema(description = "Ngày cập nhật bản ghi (nếu có)", example = "2023-10-28T11:00:00")
        LocalDateTime updatedAt
) {
}