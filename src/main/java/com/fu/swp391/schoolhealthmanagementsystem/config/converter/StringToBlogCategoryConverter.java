package com.fu.swp391.schoolhealthmanagementsystem.config.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToBlogCategoryConverter implements Converter<String, BlogCategory> {

    @Override
    public BlogCategory convert(@NonNull String source) {
        if (source.isEmpty()) {
            return null;
        }
        try {
            return BlogCategory.fromDisplayName(source);
        } catch (IllegalArgumentException e) {
            // You can log the error here if you have a logger configured
            throw e;
        }
    }
}
