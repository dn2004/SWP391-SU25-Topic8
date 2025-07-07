package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Validator để kiểm tra một ngày phải trong tương lai
 * và cách ngày hiện tại một khoảng thời gian tối thiểu
 */
public class FutureDateValidator implements ConstraintValidator<FutureDate, LocalDate> {

    private int minDays;

    @Override
    public void initialize(FutureDate constraintAnnotation) {
        this.minDays = constraintAnnotation.days();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        // Nếu giá trị là null, coi như hợp lệ (để @NotNull xử lý nếu cần thiết)
        if (value == null) {
            return true;
        }

        // Kiểm tra ngày có lớn hơn ngày hiện tại + số ngày tối thiểu không
        LocalDate minValidDate = LocalDate.now().plusDays(minDays);
        return !value.isBefore(minValidDate);
    }
}
