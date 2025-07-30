package com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease;

import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Schema(description = "DTO yêu cầu cập nhật của một bệnh mãn tính của học sinh")
@Builder
public record StudentChronicDiseaseUpdateRequestDto (
    @Schema(
            description = "Tên bệnh mãn tính",
            example = "Hen suyễn"
    )
    @Size(
            max = 150,
            message = "Tên bệnh không được vượt quá 150 ký tự"
    )
    String diseaseName,

    @Schema(
            description = "Ngày được chẩn đoán",
            example = "2020-05-10"
    )
    @PastOrPresent(message = "Ngày sinh phải là một ngày trong quá khứ hoặc hiện tại")
    LocalDate diagnosedDate,

    @Schema(
            description = "Bác sĩ chẩn đoán",
            example = "BS. Nguyễn Thị B"
    )
    @Size(
            max = 100,
            message = "Tên bác sĩ không được vượt quá 100 ký tự"
    )
    String diagnosingDoctor,

    @Schema(
            description = "Ghi chú chi tiết về bệnh, cách xử lý, hoặc thuốc men",
            example = "Cần mang theo ống hít Ventolin. Tránh các hoạt động thể chất quá sức."
    )
    @Size(
            max = 1000,
            message = "Ghi chú không được vượt quá 1000 ký tự"
    )
    String notes,

    @Schema(
            description = "File đính kèm (ví dụ: giấy khám bệnh, đơn thuốc). File mới sẽ thay thế file cũ nếu đã tồn tại."
    )
    @ValidFile(required = false, message = "File bằng chứng tiêm chủng không hợp lệ.")
    MultipartFile attachmentFile
) {}