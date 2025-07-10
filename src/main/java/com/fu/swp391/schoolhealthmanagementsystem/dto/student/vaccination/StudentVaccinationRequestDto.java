package com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination;


import com.fu.swp391.schoolhealthmanagementsystem.validation.ValidFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;

@Schema(description = "Yêu cầu tạo hoặc cập nhật thông tin tiêm chủng của học sinh")
public record StudentVaccinationRequestDto(
        @Schema(
                description = "Tên vắc-xin",
                example = "Sởi - Quai bị - Rubella (MMR)"
        )
        @NotBlank(message = "Tên vắc-xin không được để trống")
        @Size(
                max = 100,
                message = "Tên vắc-xin tối đa 100 ký tự"
        )
        String vaccineName,

        @Schema(
                description = "Ngày tiêm chủng",
                example = "2022-08-15"
        )
        @NotNull(message = "Ngày tiêm chủng không được để trống")
        @PastOrPresent(message = "Ngày sinh phải là một ngày trong quá khứ hoặc hiện tại")
        LocalDate vaccinationDate,

        @Schema(
                description = "Nơi tiêm chủng",
                example = "Trung tâm Y tế Dự phòng Quận 1"
        )
        @NotBlank(message = "Nơi tiêm chủng không được để trống")
        @Size(
                max = 200,
                message = "Nơi tiêm chủng tối đa 200 ký tự"
        )
        String provider,

        @Schema(
                description = "Ghi chú thêm",
                example = "Phản ứng nhẹ sau tiêm"
        )
        @Size(
                max = 500,
                message = "Ghi chú tối đa 500 ký tự"
        )
        String notes,

        @Schema(
                description = "File bằng chứng tiêm chủng (PDF, JPG, PNG). Bắt buộc nếu đây là yêu cầu của trường."
        )
        @ValidFile(required = true, message = "File bằng chứng tiêm chủng không hợp lệ hoặc bị thiếu.")
        MultipartFile proofFile
) {
}