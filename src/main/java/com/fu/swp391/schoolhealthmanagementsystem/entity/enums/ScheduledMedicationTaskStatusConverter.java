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
        // Store the standard enum name (e.g., "SCHEDULED") in the database.
        // This is more robust than storing the display name.
        return attribute.name();
    }

    @Override
    public ScheduledMedicationTaskStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            // Convert from the standard enum name (e.g., "SCHEDULED").
            // This aligns with how Spring MVC binds request parameters.
            return ScheduledMedicationTaskStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // This block provides backward compatibility for any existing data
            // that was stored using the display name. It can be removed
            // after migrating the data to use enum names.
            return ScheduledMedicationTaskStatus.fromDisplayName(dbData);
        }
    }
}