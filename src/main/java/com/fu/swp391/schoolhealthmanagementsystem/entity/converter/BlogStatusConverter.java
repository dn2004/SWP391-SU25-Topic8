package com.fu.swp391.schoolhealthmanagementsystem.entity.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BlogStatusConverter implements AttributeConverter<BlogStatus, String> {

    @Override
    public String convertToDatabaseColumn(BlogStatus status) {
        if (status == null) {
            return null;
        }
        return status.getDisplayName();
    }

    @Override
    public BlogStatus convertToEntityAttribute(String displayName) {
        if (displayName == null) {
            return null;
        }
        return BlogStatus.fromDisplayName(displayName);
    }
}
