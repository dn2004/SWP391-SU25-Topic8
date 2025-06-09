package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class LinkStatusConverter implements AttributeConverter<LinkStatus, String> {

    private static final Logger log = LoggerFactory.getLogger(LinkStatusConverter.class);

    @Override
    public String convertToDatabaseColumn(LinkStatus attribute) {

        if (attribute == null) {
            return null;
        }
        return attribute.getVietnameseStatus();
    }

    @Override
    public LinkStatus convertToEntityAttribute(String dbData) {

        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        try {
            // Sử dụng phương thức fromVietnameseStatus đã định nghĩa trong Enum
            return LinkStatus.fromVietnameseStatus(dbData);
        } catch (IllegalArgumentException e) {
            log.error("Lỗi: Không thể chuyển đổi giá trị '{}' từ DB thành LinkStatus. {}", dbData, e.getMessage());

            return null;

        }
    }
}