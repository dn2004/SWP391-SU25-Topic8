package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum StudentVaccinationStatus {
    PENDING("Chờ xử lý"),
    APPROVE("Chấp nhận"),
    REJECTED("Từ chối");

    private final String vietnameseName;

    StudentVaccinationStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    @JsonValue
    public String getVietnameseName() {
        return this.vietnameseName;
    }

    @JsonCreator
    public static StudentVaccinationStatus fromDisplayName(String text) {
        if (text == null || text.trim().isEmpty()) {

            log.warn("Giá trị displayName đầu vào cho StudentVaccinationStatus là null hoặc rỗng. Ném IllegalArgumentException.");
            throw new IllegalArgumentException("Trạng thái tiêm chủng không được để trống.");

        }

        for (StudentVaccinationStatus status : values()) {

            if (status.getVietnameseName().equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        log.warn("Không tìm thấy StudentVaccinationStatus nào khớp với: '{}'", text);
        throw new IllegalArgumentException("No matching status for: " + text);
    }

}