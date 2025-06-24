package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SchoolSessionConverter implements AttributeConverter<SchoolSession, String> {

    @Override
    public String convertToDatabaseColumn(SchoolSession attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public SchoolSession convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return SchoolSession.fromDisplayName(dbData);
    }
}

