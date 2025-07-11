package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Schema(description = "Trạng thái bệnh mãn tính của học sinh")
public enum StudentChronicDiseaseStatus {
    PENDING("Chờ xử lý"),
    APPROVE("Chấp nhận"),
    REJECTED("Từ chối");

    private final String displayName;

    StudentChronicDiseaseStatus(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return this.displayName;
    }

    @JsonCreator
    public static StudentChronicDiseaseStatus fromDisplayName(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái bệnh mãn tính không được để trống.");
        }

        for (StudentChronicDiseaseStatus status : values()) {
            if (status.getDisplayName().equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy trạng thái nào khớp với: " + text);
    }
}

