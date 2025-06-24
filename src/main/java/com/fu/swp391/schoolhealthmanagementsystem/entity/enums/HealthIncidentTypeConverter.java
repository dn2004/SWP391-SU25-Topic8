package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class HealthIncidentTypeConverter implements AttributeConverter<HealthIncidentType, String> {

    @Override
    public String convertToDatabaseColumn(HealthIncidentType healthIncidentType) {
        return healthIncidentType == null ? null : healthIncidentType.name();
    }

    @Override
    public HealthIncidentType convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : HealthIncidentType.valueOf(dbData);
    }
}
