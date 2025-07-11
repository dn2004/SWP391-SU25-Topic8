package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender gender) {
        if (gender == null) {
            return null;
        }
        return gender.getDisplayName();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        log.info("convertToEntityAttribute - input dbData: {}", dbData);
        if (dbData == null) {
            log.info("convertToEntityAttribute - return: null");
            return null;
        }
        Gender gender = Gender.fromDisplayName(dbData);
        log.info("convertToEntityAttribute - return: {}", gender);
        return gender;
    }
}
