package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToBlogStatusConverter implements Converter<String, BlogStatus> {

    @Override
    public BlogStatus convert(@NonNull String source) {
        if (source.isEmpty()) {
            return null;
        }
        try {
            return BlogStatus.fromDisplayName(source);
        } catch (IllegalArgumentException e) {
            // You can log the error here if you have a logger configured
            throw e;
        }
    }
}

