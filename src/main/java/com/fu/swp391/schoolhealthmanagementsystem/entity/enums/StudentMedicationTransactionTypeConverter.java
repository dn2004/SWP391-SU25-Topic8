package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class StudentMedicationTransactionTypeConverter implements AttributeConverter<StudentMedicationTransactionType, String> {

    @Override
    public String convertToDatabaseColumn(StudentMedicationTransactionType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public StudentMedicationTransactionType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return StudentMedicationTransactionType.fromDisplayName(dbData);
    }
}

