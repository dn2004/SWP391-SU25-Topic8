package com.fu.swp391.schoolhealthmanagementsystem.entity.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VaccinationCampaignStatusConverter implements AttributeConverter<VaccinationCampaignStatus, String> {

    @Override
    public String convertToDatabaseColumn(VaccinationCampaignStatus status) {
        return status == null ? null : status.name();
    }

    @Override
    public VaccinationCampaignStatus convertToEntityAttribute(String dbData) {
        return dbData == null || dbData.isEmpty() ? null : VaccinationCampaignStatus.valueOf(dbData);
    }
}
