package com.fu.swp391.schoolhealthmanagementsystem.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl; // Để truy cập giá trị trường động

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, Object> {

    private String passwordField;
    private String confirmPasswordField;
    private String message;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        this.passwordField = constraintAnnotation.passwordField();
        this.confirmPasswordField = constraintAnnotation.confirmPasswordField();
        this.message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Lấy giá trị của hai trường từ đối tượng 'value' (chính là RegisterParentRequestDto)
        Object passwordValue = new BeanWrapperImpl(value).getPropertyValue(passwordField);
        Object confirmPasswordValue = new BeanWrapperImpl(value).getPropertyValue(confirmPasswordField);

        boolean isValid = (passwordValue == null && confirmPasswordValue == null) ||
                (passwordValue != null && passwordValue.equals(confirmPasswordValue));

        if (!isValid) {
            // Nếu không hợp lệ, thêm lỗi vào trường confirmPassword
            // (hoặc một node lỗi chung nếu muốn)
            context.disableDefaultConstraintViolation(); // Tắt message mặc định
            context.buildConstraintViolationWithTemplate(message) // Sử dụng message từ annotation
                    .addPropertyNode(confirmPasswordField) // Gán lỗi cho trường confirmPassword
                    .addConstraintViolation();
        }
        return isValid;
    }
}