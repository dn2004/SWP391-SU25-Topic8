package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class StringToBlogCategoryConverter implements Converter<String, BlogCategory> {

    @Override
    public BlogCategory convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành BlogCategory", source);
        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho BlogCategory.");
            return null;
        }
        try {
            BlogCategory result = BlogCategory.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành BlogCategory. Lỗi: {}", source, e.getMessage());
            throw e;
        }
    }
}
