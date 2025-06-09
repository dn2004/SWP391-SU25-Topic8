package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@Schema(
        description = "Trạng thái tiêm chủng của học sinh",
        allowableValues = {"PENDING", "APPROVE", "REJECTED"}
)
public enum StudentVaccinationStatus {
    PENDING("Chờ xử lý"),
    APPROVE("Chấp nhận"),
    REJECTED("Từ chối");

    private final String vietnameseName;

    StudentVaccinationStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public static StudentVaccinationStatus fromDisplayName(String text) {
        if (text == null || text.trim().isEmpty()) {

            log.warn("Giá trị displayName đầu vào cho StudentVaccinationStatus là null hoặc rỗng. Ném IllegalArgumentException.");
            throw new IllegalArgumentException("Trạng thái tiêm chủng không được để trống.");

        }

        for (StudentVaccinationStatus b : StudentVaccinationStatus.values()) {

            if (b.vietnameseName.equalsIgnoreCase(text)) {
                return b;
            }
            if (b.name().equalsIgnoreCase(text)) {
                log.warn("Tìm thấy StudentVaccinationStatus bằng tên hằng số '{}' thay vì vietnameseName. Giá trị từ DB/request có thể không phải tiếng Việt.", text);
                return b;
            }
        }
        log.warn("Không tìm thấy StudentVaccinationStatus cho giá trị displayName: '{}'. Ném IllegalArgumentException.", text);
        throw new IllegalArgumentException("Không tìm thấy trạng thái của thông tin này: " + text);
    }

    public String getVietnameseName() {
        return vietnameseName;
    }
}