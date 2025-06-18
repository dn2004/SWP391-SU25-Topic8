package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.CreateStudentMedicationByStaffRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class StartDateBeforeExpiryDateValidator implements ConstraintValidator<StartDateBeforeExpiryDate, CreateStudentMedicationByStaffRequestDto> {

    @Override
    public void initialize(StartDateBeforeExpiryDate constraintAnnotation) {
        // Không cần khởi tạo gì
    }

    @Override
    public boolean isValid(CreateStudentMedicationByStaffRequestDto dto, ConstraintValidatorContext context) {
        // Nếu expiryDate là null thì không cần kiểm tra
        if (dto.expiryDate() == null) {
            return true;
        }

        // Nếu scheduleStartDate là null, kết quả sẽ phụ thuộc vào @NotNull tại field đó
        if (dto.scheduleStartDate() == null) {
            return true;
        }

        // Kiểm tra scheduleStartDate phải nghiêm túc trước expiryDate (không được bằng)
        return dto.scheduleStartDate().isBefore(dto.expiryDate());
    }
}
