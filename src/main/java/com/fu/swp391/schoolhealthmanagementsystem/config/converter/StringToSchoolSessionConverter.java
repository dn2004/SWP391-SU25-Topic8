package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToSchoolSessionConverter implements Converter<String, SchoolSession> {

    @Override
    public SchoolSession convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành SchoolSession", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho SchoolSession.");
            return null;
        }

        try {
            SchoolSession result = SchoolSession.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành SchoolSession. Lỗi: {}",
                    source, e.getMessage());
            throw e;
        }
    }
}
