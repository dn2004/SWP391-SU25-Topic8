package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
public class StudentChronicDiseaseStatusConverter implements AttributeConverter<StudentChronicDiseaseStatus, String> {

    @Override
    public String convertToDatabaseColumn(StudentChronicDiseaseStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public StudentChronicDiseaseStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return StudentChronicDiseaseStatus.fromDisplayName(dbData);
        } catch (IllegalArgumentException e) {
            log.error("Error: Could not convert value '{}' from DB to StudentChronicDiseaseStatus. {}", dbData, e.getMessage());
            return null; // Or throw an exception, depending on desired behavior
        }
    }
}

