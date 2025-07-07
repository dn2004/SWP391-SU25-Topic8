package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ConsentStatusConverter implements AttributeConverter<ConsentStatus, String> {

    @Override
    public String convertToDatabaseColumn(ConsentStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public ConsentStatus convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : ConsentStatus.valueOf(dbData);
    }
}
