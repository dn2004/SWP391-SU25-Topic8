package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToBlogStatusConverter implements Converter<String, BlogStatus> {

    @Override
    public BlogStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành BlogStatus", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho BlogStatus.");
            return null;
        }
        try {
            BlogStatus result = BlogStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành BlogStatus. Lỗi: {}", source, e.getMessage());
            throw e;
        }
    }
}
