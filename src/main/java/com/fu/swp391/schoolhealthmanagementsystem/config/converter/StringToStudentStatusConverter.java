package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter to convert string representation of StudentStatus in URL path variables to the actual enum value.
 */
@Component
public class StringToStudentStatusConverter implements Converter<String, StudentStatus> {

    @Override
    public StudentStatus convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        StudentStatus status = StudentStatus.fromString(source);
        if (status == null) {
            throw new IllegalArgumentException("Unknown StudentStatus: " + source);
        }
        return status;
    }
}
