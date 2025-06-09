package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;


@Converter(autoApply = true)
@Slf4j
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public Gender convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        try {
            return Gender.fromDisplayName(dbData);
        } catch (IllegalArgumentException e) {
            log.error("Lỗi: Không thể chuyển đổi giá trị '{}' từ DB thành Gender. {}", dbData, e.getMessage());
            return null;
        }
    }
}
