package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StringToLinkStatusConverter implements Converter<String, LinkStatus> {

    @Override
    public LinkStatus convert(@NonNull String source) {
        log.debug("Đang chuyển đổi chuỗi '{}' thành LinkStatus", source);

        if (source.isEmpty()) {
            log.warn("Chuỗi nguồn rỗng. Trả về null cho LinkStatus.");
            return null;
        }

        try {
            LinkStatus result = LinkStatus.fromDisplayName(source);
            log.debug("Đã chuyển đổi thành công '{}' thành enum '{}'", source, result);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Chuyển đổi không thành công cho chuỗi '{}' thành LinkStatus. Lỗi: {}",
                    source, e.getMessage());
            throw e;
        }
    }
}
