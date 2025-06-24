package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToScheduledMedicationTaskStatusConverter implements Converter<String, ScheduledMedicationTaskStatus> {

    @Override
    public ScheduledMedicationTaskStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành ScheduledMedicationTaskStatus", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho ScheduledMedicationTaskStatus.");
            return null;
        }

        try {
            ScheduledMedicationTaskStatus result = ScheduledMedicationTaskStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành ScheduledMedicationTaskStatus. Lỗi: {}",
                    source, e.getMessage());
            throw e;
        }
    }
}
