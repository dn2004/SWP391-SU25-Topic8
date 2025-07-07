package com.fu.swp391.schoolhealthmanagementsystem.validation;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator để kiểm tra tính hợp lệ của tên lớp học mầm non
 * Hỗ trợ 3 khối chính: Mầm, Chồi, Lá với các lớp A-H
 */
public class ClassNameValidator implements ConstraintValidator<ValidClassName, String> {

    // Regex cho phép các định dạng: Mầm A, Chồi B, Lá C, v.v.
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
            "^(MẦM|MAM|CHỒI|CHOI|LÁ|LA)\\s*([A-H])$",
            Pattern.CASE_INSENSITIVE);

    @Override
    public void initialize(ValidClassName constraintAnnotation) {
        // Không cần khởi tạo gì thêm
    }

    @Override
    public boolean isValid(String className, ConstraintValidatorContext context) {
        // Nếu className là null thì để cho @NotNull hoặc @NotBlank xử lý
        if (className == null || className.trim().isEmpty()) {
            return true;
        }

        // Kiểm tra tên lớp có đúng định dạng không
        return CLASS_NAME_PATTERN.matcher(className.trim().toUpperCase()).matches();
    }

    /**
     * Phương thức tĩnh để xác định ClassGroup từ tên lớp
     * @param className Tên lớp học (vd: "Mầm A", "Chồi B", "Lá C")
     * @return ClassGroup tương ứng hoặc null nếu không xác định được
     */
    public static ClassGroup determineClassGroup(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        String normalizedClassName = className.trim().toUpperCase();

        if (normalizedClassName.startsWith("MẦM") || normalizedClassName.startsWith("MAM")) {
            return ClassGroup.MAM;
        } else if (normalizedClassName.startsWith("CHỒI") || normalizedClassName.startsWith("CHOI")) {
            return ClassGroup.CHOI;
        } else if (normalizedClassName.startsWith("LÁ") || normalizedClassName.startsWith("LA")) {
            return ClassGroup.LA;
        }

        return null;
    }

    /**
     * Phương thức tĩnh để xác định Class từ tên lớp
     * @param className Tên lớp học (vd: "Mầm A", "Chồi B", "Lá C")
     * @return Class tương ứng hoặc null nếu không xác định được
     */
    public static Class determineClass(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        Matcher matcher = CLASS_NAME_PATTERN.matcher(className.trim().toUpperCase());
        if (matcher.find() && matcher.groupCount() >= 2) {
            String classLetter = matcher.group(2);
            return Class.valueOf(classLetter);
        }

        return null;
    }
}
