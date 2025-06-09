package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;


import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true) // Tự động áp dụng cho các thuộc tính kiểu UserRole
public class UserRoleConverter implements AttributeConverter<UserRole, String> {

    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        // Khi lưu vào DB, lấy giá trị vietnameseName từ enum
        if (attribute == null) {
            return null;
        }
        return attribute.getVietnameseName();
    }

    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        // Khi đọc từ DB, chuyển chuỗi vietnameseName (dbData) trở lại thành enum UserRole

        if (dbData == null || dbData.trim().isEmpty()) {
            // Nếu cột trong DB có thể NULL và bạn muốn thuộc tính Entity cũng là null
            return null;
        }

        try {
            // Sử dụng phương thức fromVietnameseName đã định nghĩa trong Enum
            return UserRole.fromVietnameseName(dbData);
        } catch (IllegalArgumentException e) {
            log.error("Lỗi: Không thể chuyển đổi giá trị '{}' từ DB thành UserRole. {}", dbData, e.getMessage());
            // Quyết định hành vi khi giá trị từ DB không hợp lệ:
            // 1. Ném một exception khác để dừng tiến trình (nếu dữ liệu phải luôn hợp lệ)
            //    throw new IllegalStateException("Giá trị vai trò người dùng không hợp lệ trong DB: '" + dbData + "'", e);
            // 2. Trả về null (nếu thuộc tính có thể null và đây là hành vi chấp nhận được)
            return null;
            // 3. Trả về một vai trò mặc định (nếu có và phù hợp)
            //    ví dụ: return UserRole.Parent; (Cẩn thận với logic này)
        }
    }
}
