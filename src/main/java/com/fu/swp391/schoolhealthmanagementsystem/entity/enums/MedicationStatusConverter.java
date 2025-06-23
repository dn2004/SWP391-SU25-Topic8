package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MedicationStatusConverter implements AttributeConverter<MedicationStatus, String> {

    @Override
    public String convertToDatabaseColumn(MedicationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public MedicationStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return MedicationStatus.fromDisplayName(dbData);
    }
}

