package com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;

@Schema(description = "Thông tin yêu cầu cho nhân viên y tế thực hiện cho uống thuốc")
public record AdministerMedicationRequestDto(
        @Schema(
                description = "Thời điểm thực tế cho uống thuốc (HH:mm or HH:mm:ss)",
                example = "08:30:00"
        )
        @NotNull(message = "Thời điểm thực tế cho uống thuốc không được để trống")
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
        LocalTime administeredTime,

        @Schema(
                description = "Ghi chú của nhân viên y tế",
                example = "Đã kiểm tra nhiệt độ trước khi uống thuốc"
        )
        @Size(
                max = 1000,
                message = "Ghi chú của nhân viên y tế không quá 1000 ký tự"
        )
        String staffNotes,

        @Schema(description = "File bằng chứng (ảnh, video, pdf)") MultipartFile proofFile
) {}
