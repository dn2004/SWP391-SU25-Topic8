package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Schema(description = "Trạng thái của chiến dịch tiêm chủng")
@RequiredArgsConstructor
public enum VaccinationCampaignStatus {
    DRAFT("Nháp"),           // Trạng thái ban đầu khi chiến dịch đang được tạo
    SCHEDULED("Đã lên lịch"), // Chiến dịch đã lên lịch và đang gửi/thu thập phiếu đồng ý
    PREPARING("Đang chuẩn bị"),    // Không còn tiếp nhận phiếu đồng ý, chuẩn bị cho ngày tiêm chủng
    IN_PROGRESS("Đang diễn ra"), // Tiêm chủng đang diễn ra
    COMPLETED("Đã hoàn thành"),   // Tất cả các mũi tiêm đã được thực hiện
    CANCELED("Đã hủy");       // Chiến dịch đã bị hủy

    private final String displayName;

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static VaccinationCampaignStatus fromDisplayName(String displayName) {
        if (displayName == null || displayName.isEmpty()) {
            throw new IllegalArgumentException("Display name không được để trống");
        }
        for (VaccinationCampaignStatus status : VaccinationCampaignStatus.values()) {
            if (status.displayName.equalsIgnoreCase(displayName) || status.name().equalsIgnoreCase(displayName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy VaccinationCampaignStatus với displayName: " + displayName);
    }
}
