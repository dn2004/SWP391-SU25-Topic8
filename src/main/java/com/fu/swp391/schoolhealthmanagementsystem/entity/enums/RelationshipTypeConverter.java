package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // autoApply=true để JPA tự động áp dụng converter này
// cho tất cả các thuộc tính kiểu RelationshipType trong các Entity.
public class RelationshipTypeConverter implements AttributeConverter<RelationshipType, String> {

    @Override
    public String convertToDatabaseColumn(RelationshipType attribute) {
        // Khi lưu vào DB, lấy giá trị tiếng Việt từ enum
        if (attribute == null) {
            return null;
        }
        return attribute.getDisplayName();
    }

    @Override
    public RelationshipType convertToEntityAttribute(String dbData) {
        // Khi đọc từ DB, chuyển chuỗi tiếng Việt (dbData) trở lại thành enum
        // Sử dụng phương thức fromString đã có trong Enum của bạn.
        // Nếu dbData là null hoặc rỗng, fromString sẽ xử lý (ví dụ: trả về null hoặc OTHER).
        if (dbData == null || dbData.trim().isEmpty()) {
            return null; // Hoặc giá trị mặc định nếu muốn
        }
        return RelationshipType.fromDisplayName(dbData);
    }
}
