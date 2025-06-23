package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LinkStatusConverter implements AttributeConverter<LinkStatus, String> {

    @Override
    public String convertToDatabaseColumn(LinkStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public LinkStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LinkStatus.valueOf(dbData);
    }
}