package com.fu.swp391.schoolhealthmanagementsystem.validation; // Hoặc package của bạn

import com.fu.swp391.schoolhealthmanagementsystem.util.DateUtils; // Import class DateUtils
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class IsWorkdayValidator implements ConstraintValidator<IsWorkday, LocalDate> {

    @Override
    public void initialize(IsWorkday constraintAnnotation) {
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // @NotNull sẽ xử lý nếu date là bắt buộc. Nếu date được phép null, thì null là hợp lệ với IsWorkday.
        }
        return DateUtils.isWorkday(date);
    }
}