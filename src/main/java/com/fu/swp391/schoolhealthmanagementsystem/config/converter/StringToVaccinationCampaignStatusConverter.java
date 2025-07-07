package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToVaccinationCampaignStatusConverter implements Converter<String, VaccinationCampaignStatus> {

    @Override
    public VaccinationCampaignStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành VaccinationCampaignStatus", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho VaccinationCampaignStatus.");
            return null;
        }

        try {
            // Sử dụng phương thức fromDisplayName để hỗ trợ cả tên enum và tên hiển thị
            VaccinationCampaignStatus result = VaccinationCampaignStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành VaccinationCampaignStatus. Lỗi: {}",
                    source, e.getMessage());
            // Ném lại ngoại lệ để Spring biết rằng việc chuyển đổi đã thất bại
            throw new IllegalArgumentException("Không tìm thấy trạng thái chiến dịch tiêm chủng phù hợp cho giá trị: " + source);
        }
    }
}
