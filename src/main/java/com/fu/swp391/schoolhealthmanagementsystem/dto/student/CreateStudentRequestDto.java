package com.fu.swp391.schoolhealthmanagementsystem.dto.student;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Yêu cầu tạo mới thông tin học sinh")
public record CreateStudentRequestDto(
        @Schema(
                description = "Họ và tên đầy đủ của học sinh",
                example = "Nguyễn Văn A"
        )
        @NotBlank(message = "Họ và tên không được để trống")
        @Size(
                max = 100,
                message = "Họ và tên tối đa 100 ký tự"
        )
        String fullName,

        @Schema(
                description = "Ngày sinh của học sinh",
                example = "2020-08-20"
        )
        @NotNull(message = "Ngày sinh không được để trống")
        @PastOrPresent(message = "Ngày sinh phải là một ngày trong quá khứ hoặc hiện tại")
        LocalDate dateOfBirth,

        @Schema(
                description = "Giá trị giới tính bao gồm: Nam/Nữ",
                example = "Nam"
        )
        @NotNull(message = "Giới tính không được để trống")
        Gender gender,

        @Schema(
                description = "Khối lớp (MAM, CHOI, LA)",
                example = "MAM"
        )
        @NotNull(message = "Khối lớp không được để trống")
        ClassGroup classGroup,

        @Schema(
                description = "Lớp (A, B, C, D, E, F, G, H)",
                example = "A"
        )
        @NotNull(message = "Lớp không được để trống")
        Class classValue
) {}
