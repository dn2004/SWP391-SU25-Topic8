package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import lombok.Getter;

@Getter // Để dễ dàng lấy displayValue
public enum SupplyTransactionType {
    RECEIVED("Nhập kho mới"),
    USED_FOR_INCIDENT("Sử dụng cho sự cố"),
    ADJUSTMENT_OUT("Điều chỉnh giảm"), // Ví dụ: hỏng, hết hạn
    ADJUSTMENT_IN("Điều chỉnh tăng"), // Ví dụ: kiểm kê thừa
    RETURN_FROM_INCIDENT("Trả lại từ sự cố");

    private final String displayValue;

    SupplyTransactionType(String displayValue) {
        this.displayValue = displayValue;
    }

    // Optional: Phương thức để tìm Enum từ displayValue (hữu ích cho việc parse từ UI nếu cần)
    public static SupplyTransactionType fromDisplayValue(String displayValue) {
        for (SupplyTransactionType type : SupplyTransactionType.values()) {
            if (type.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy loại giao dịch vật tư với giá trị hiển thị: " + displayValue);
    }

    // Optional: Phương thức để tìm Enum từ name (giá trị lưu trong DB)
    public static SupplyTransactionType fromName(String name) {
        try {
            return SupplyTransactionType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Không tìm thấy loại giao dịch vật tư với tên: " + name, e);
        }
    }
}