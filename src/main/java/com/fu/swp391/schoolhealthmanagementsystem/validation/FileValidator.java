package com.fu.swp391.schoolhealthmanagementsystem.validation;


import com.fu.swp391.schoolhealthmanagementsystem.prop.FileStorageProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component // Để có thể inject FileStorageProperties
public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    @Autowired
    private FileStorageProperties fileStorageProperties; // Inject properties

    private boolean isRequired;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.isRequired = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return !isRequired; // Nếu không bắt buộc và file null/rỗng -> hợp lệ
        }

        // Kiểm tra kích thước
        if (file.getSize() > fileStorageProperties.maxProofSizeBytes()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Kích thước file vượt quá giới hạn cho phép (" + (fileStorageProperties.maxProofSizeBytes() / (1024 * 1024)) + "MB)")
                    .addConstraintViolation();
            return false;
        }

        // Kiểm tra loại file
        String contentType = file.getContentType();
        if (contentType == null || !fileStorageProperties.allowedTypes().contains(contentType.toLowerCase())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Loại file không được hỗ trợ. Chỉ chấp nhận: " + String.join(", ", fileStorageProperties.allowedTypes()))
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}