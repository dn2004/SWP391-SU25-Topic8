package com.fu.swp391.schoolhealthmanagementsystem.entity.converter;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BlogCategoryConverter implements AttributeConverter<BlogCategory, String> {

    @Override
    public String convertToDatabaseColumn(BlogCategory category) {
        if (category == null) {
            return null;
        }
        return category.getDisplayName();
    }

    @Override
    public BlogCategory convertToEntityAttribute(String displayName) {
        if (displayName == null) {
            return null;
        }
        return BlogCategory.fromDisplayName(displayName);
    }
}
