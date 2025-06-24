package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return UserRole.fromDisplayName(dbData);
    }
}

