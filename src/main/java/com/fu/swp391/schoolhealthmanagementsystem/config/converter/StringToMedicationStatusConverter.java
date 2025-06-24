package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToMedicationStatusConverter implements Converter<String, MedicationStatus> {

    @Override
    public MedicationStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành MedicationStatus", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho MedicationStatus.");
            return null;
        }

        try {
            MedicationStatus result = MedicationStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành MedicationStatus. Lỗi: {}",
                    source, e.getMessage());
            throw e;
        }
    }
}
