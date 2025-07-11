package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.UpdateVaccinationConsentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.VaccinationConsentResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationConsent;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VaccinationConsentMapper {

    @Mapping(source = "campaign.campaignId", target = "campaignId")
    @Mapping(source = "campaign.campaignName", target = "campaignName")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(source = "student.className", target = "studentClass")
    @Mapping(source = "parent.userId", target = "parentId")
    @Mapping(source = "parent.fullName", target = "parentName")
    @Mapping(source = "medicalNotes", target = "medicalNotes")
    VaccinationConsentResponseDto toDto(VaccinationConsent entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "consentId", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "consentFormSentAt", ignore = true)
    @Mapping(target = "responseReceivedAt", ignore = true)
    @Mapping(target = "reminderSentAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(UpdateVaccinationConsentRequestDto dto, @MappingTarget VaccinationConsent entity);
}
