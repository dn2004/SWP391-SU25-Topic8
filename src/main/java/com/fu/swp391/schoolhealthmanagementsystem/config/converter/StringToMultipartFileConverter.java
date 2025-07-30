package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
public class StringToMultipartFileConverter implements Converter<String, MultipartFile> {

    @Override
    public MultipartFile convert(@NonNull String source) {
        // Nếu client gửi lên một chuỗi rỗng cho trường file (khi không chọn file),
        // hãy diễn giải nó là null thay vì gây ra lỗi chuyển đổi.
        if (!StringUtils.hasText(source)) {
            return null;
        }
        // Trường hợp này không nên xảy ra trong thực tế với multipart,
        // nhưng đây là một fallback an toàn.
        // Không có cách hợp lý để tạo một file thực sự từ một chuỗi bất kỳ.
        return null;
    }
}
