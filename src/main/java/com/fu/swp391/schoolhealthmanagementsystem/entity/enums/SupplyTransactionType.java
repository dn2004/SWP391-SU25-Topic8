package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter // Để dễ dàng lấy displayName
@Schema(description = "Loại giao dịch vật tư")
public enum SupplyTransactionType {
    RECEIVED("Nhập kho mới"),
    USED_FOR_INCIDENT("Sử dụng cho sự cố"),
    ADJUSTMENT_OUT("Điều chỉnh giảm"), // Ví dụ: hỏng, hết hạn
    ADJUSTMENT_IN("Điều chỉnh tăng"), // Ví dụ: kiểm kê thừa
    RETURN_FROM_INCIDENT("Trả lại từ sự cố"), EXPIRED("Loại bỏ hết vì hết hạn");

    private final String displayName;

    SupplyTransactionType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static SupplyTransactionType fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (SupplyTransactionType type : SupplyTransactionType.values()) {
            if (type.getDisplayName().equalsIgnoreCase(displayName) || type.name().equalsIgnoreCase(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy loại giao dịch vật tư với giá trị hiển thị: " + displayName);
    }
}