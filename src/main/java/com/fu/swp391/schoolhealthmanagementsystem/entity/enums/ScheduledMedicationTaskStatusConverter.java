package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ScheduledMedicationTaskStatusConverter implements AttributeConverter<ScheduledMedicationTaskStatus, String> {

    @Override
    public String convertToDatabaseColumn(ScheduledMedicationTaskStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public ScheduledMedicationTaskStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return ScheduledMedicationTaskStatus.fromDisplayName(dbData);
    }
}

