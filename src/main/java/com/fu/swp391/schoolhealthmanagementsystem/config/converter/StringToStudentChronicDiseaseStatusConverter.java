package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToStudentChronicDiseaseStatusConverter implements Converter<String, StudentChronicDiseaseStatus> {

    @Override
    public StudentChronicDiseaseStatus convert(@NonNull String source) {
        log.debug("Converting string '{}' to StudentChronicDiseaseStatus", source);

        if (source.isEmpty()) {
            log.warn("Source string is empty. Returning null for StudentChronicDiseaseStatus.");
            return null;
        }

        try {
            StudentChronicDiseaseStatus result = StudentChronicDiseaseStatus.fromDisplayName(source);
            log.debug("Successfully converted '{}' to enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert string '{}' to StudentChronicDiseaseStatus. Error: {}",
                    source, e.getMessage());
            throw e;
        }
    }
}

