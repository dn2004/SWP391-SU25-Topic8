package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SupplyTransactionTypeConverter implements AttributeConverter<SupplyTransactionType, String> {

    @Override
    public String convertToDatabaseColumn(SupplyTransactionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public SupplyTransactionType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return SupplyTransactionType.fromDisplayName(dbData);
    }
}

