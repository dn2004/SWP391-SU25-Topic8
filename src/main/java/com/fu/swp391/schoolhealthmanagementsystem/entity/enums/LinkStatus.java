package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema; // Nếu bạn muốn dùng Swagger
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Schema(description = "Trạng thái liên kết", allowableValues = {"ACTIVE", "INACTIVE"}) // Ví dụ nếu dùng Swagger
public enum LinkStatus {
    ACTIVE("Hoạt động"),
    INACTIVE("Không hoạt động");

    private final String vietnameseStatus;

    LinkStatus(String vietnameseStatus) {
        this.vietnameseStatus = vietnameseStatus;
    }

    public static LinkStatus fromVietnameseStatus(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Giá trị vietnameseStatus đầu vào cho LinkStatus là null hoặc rỗng. Xem xét trả về null hoặc giá trị mặc định nếu có.");
            // Tùy thuộc vào yêu cầu nghiệp vụ, bạn có thể:
            // 1. Trả về null nếu thuộc tính này có thể null trong DB/Entity
            // return null;
            // 2. Ném lỗi nếu giá trị này là bắt buộc
            throw new IllegalArgumentException("Trạng thái tiếng Việt không được để trống cho LinkStatus.");
            // 3. Trả về một giá trị mặc định (nếu có LinkStatus.UNKNOWN chẳng hạn)
        }

        for (LinkStatus status : LinkStatus.values()) {
            if (status.vietnameseStatus.equalsIgnoreCase(text)) {
                return status;
            }
            // Có thể thêm tìm kiếm theo tên enum constant nếu cần
            if (status.name().equalsIgnoreCase(text)) {
                log.warn("Tìm thấy LinkStatus bằng tên hằng số '{}' thay vì vietnameseStatus. Điều này có thể không mong muốn nếu bạn luôn muốn lưu/đọc bằng tiếng Việt.", text);
                return status;
            }
        }

        log.warn("Không tìm thấy LinkStatus cho giá trị vietnameseStatus: '{}'. Ném IllegalArgumentException.", text);
        throw new IllegalArgumentException("Không tìm thấy trạng thái liên kết với tên: " + text);
    }
    // Lombok @Getter đã tự động tạo getVietnameseStatus()
}