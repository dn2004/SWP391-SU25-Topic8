package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender gender) {
        if (gender == null) {
            return null;
        }
        return gender.getDisplayName();
    }

    @Override
    public Gender convertToEntityAttribute(String displayName) {
        if (displayName == null) {
            return null;
        }
        return Gender.fromDisplayName(displayName);
    }
}

