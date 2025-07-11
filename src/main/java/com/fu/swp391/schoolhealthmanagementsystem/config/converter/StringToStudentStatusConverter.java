package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import io.micrometer.common.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a string representation of StudentStatus from URL path variables to the corresponding enum value.
 */
@Component
@Slf4j
public class StringToStudentStatusConverter implements Converter<String, StudentStatus> {

    @Override
    public StudentStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành StudentStatus", source);

        if (source.trim().isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho StudentStatus.");
            return null;
        }
        try {
            StudentStatus result = StudentStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành StudentStatus. Lỗi: {}", source, e.getMessage());
            throw e;
        }
    }
}