package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToStudentVaccinationStatusConverter implements Converter<String, StudentVaccinationStatus> {

    @Override
    public StudentVaccinationStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành StudentVaccinationStatus", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho StudentVaccinationStatus.");
            return null;
        }

        try {
            StudentVaccinationStatus result = StudentVaccinationStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành StudentVaccinationStatus. Lỗi: {}",
                    source, e.getMessage());
            throw e;
        }
    }
}
