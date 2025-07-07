package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SchoolVaccinationStatusConverter implements AttributeConverter<SchoolVaccinationStatus, String> {

    @Override
    public String convertToDatabaseColumn(SchoolVaccinationStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public SchoolVaccinationStatus convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : SchoolVaccinationStatus.valueOf(dbData);
    }
}
