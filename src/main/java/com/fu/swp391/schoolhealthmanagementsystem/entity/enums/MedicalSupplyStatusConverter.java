package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA AttributeConverter to convert MedicalSupplyStatus enum to String in database
 * and vice versa.
 */
@Converter(autoApply = true)
public class MedicalSupplyStatusConverter implements AttributeConverter<MedicalSupplyStatus, String> {

    @Override
    public String convertToDatabaseColumn(MedicalSupplyStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public MedicalSupplyStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return MedicalSupplyStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            // Log warning nếu cần và trả về giá trị mặc định
            return MedicalSupplyStatus.AVAILABLE;
        }
    }
}
