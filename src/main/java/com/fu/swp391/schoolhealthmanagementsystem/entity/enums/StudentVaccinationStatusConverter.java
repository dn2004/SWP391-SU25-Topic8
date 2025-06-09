package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
public class StudentVaccinationStatusConverter implements AttributeConverter<StudentVaccinationStatus, String> {



    @Override
    public String convertToDatabaseColumn(StudentVaccinationStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getVietnameseName();
    }

    @Override
    public StudentVaccinationStatus convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            // Sử dụng phương thức fromDisplayName đã định nghĩa trong Enum
            return StudentVaccinationStatus.fromDisplayName(dbData);
        } catch (IllegalArgumentException e) {
            log.error("Lỗi: Không thể chuyển đổi giá trị '{}' từ DB thành StudentVaccinationStatus. {}", dbData, e.getMessage());
            return null;

        }
    }
}
