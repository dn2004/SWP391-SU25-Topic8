package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ClassGroupConverter implements AttributeConverter<ClassGroup, String> {

    @Override
    public String convertToDatabaseColumn(ClassGroup classGroup) {
        if (classGroup == null) {
            return null;
        }
        return classGroup.getDisplayName();
    }

    @Override
    public ClassGroup convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return ClassGroup.fromString(dbData);
    }
}

