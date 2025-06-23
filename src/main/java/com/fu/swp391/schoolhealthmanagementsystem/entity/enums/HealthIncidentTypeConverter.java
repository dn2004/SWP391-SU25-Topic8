package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class HealthIncidentTypeConverter implements AttributeConverter<HealthIncidentType, String> {

    @Override
    public String convertToDatabaseColumn(HealthIncidentType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public HealthIncidentType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return HealthIncidentType.fromDisplayName(dbData);
    }
}

