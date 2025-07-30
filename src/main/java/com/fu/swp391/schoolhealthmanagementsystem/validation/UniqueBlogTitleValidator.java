package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.repository.BlogRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniqueBlogTitleValidator implements ConstraintValidator<UniqueBlogTitle, String> {
    @Autowired
    private BlogRepository blogRepository;

    @Override
    public boolean isValid(String title, ConstraintValidatorContext context) {
        if (title == null || title.trim().isEmpty()) {
            return true; // Để NotEmpty xử lý
        }
        return !blogRepository.existsByTitleIgnoreCase(title.trim());
    }
}

