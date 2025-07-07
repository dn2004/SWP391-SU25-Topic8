package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToClassGroupConverter implements Converter<String, ClassGroup> {

    @Override
    public ClassGroup convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành ClassGroup", source);

        if (source.trim().isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho ClassGroup.");
            return null;
        }

        try {
            // Sử dụng phương thức fromString đã có trong enum ClassGroup
            ClassGroup result = ClassGroup.fromString(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành ClassGroup. Lỗi: {}",
                    source, e.getMessage());
            // Ném lại ngoại lệ để Spring biết rằng việc chuyển đổi đã thất bại
            throw new IllegalArgumentException("Không tìm thấy khối lớp phù hợp cho giá trị: " + source);
        }
    }
}

