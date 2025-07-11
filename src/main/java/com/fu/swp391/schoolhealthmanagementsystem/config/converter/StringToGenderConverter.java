package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToGenderConverter implements Converter<String, Gender> {

    @Override
    public Gender convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành Gender", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho Gender.");
            return null;
        }

        try {
            Gender result = Gender.fromDisplayName(source.trim());
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành Gender. Lỗi: {}", source, e.getMessage());
            // Throw a more descriptive exception for Spring to handle
            throw new IllegalArgumentException(String.format("Giá trị '%s' không hợp lệ cho Gender. Các giá trị được chấp nhận: 'Nam', 'Nữ', 'MALE', 'FEMALE'", source), e);
        }
    }
}
