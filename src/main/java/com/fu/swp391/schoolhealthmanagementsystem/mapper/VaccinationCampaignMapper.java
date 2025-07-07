package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.CreateVaccinationCampaignRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.VaccinationCampaignResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VaccinationCampaignMapper {

    @Mapping(target = "campaignId", ignore = true)
    @Mapping(target = "organizedByUser", ignore = true)
    @Mapping(target = "consentForms", ignore = true)
    @Mapping(target = "vaccinations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    VaccinationCampaign toEntity(CreateVaccinationCampaignRequestDto dto);

    @Mapping(source = "organizedByUser.userId", target = "organizedByUserId")
    @Mapping(source = "organizedByUser.fullName", target = "organizedByUserName")
    @Mapping(source = "updatedByUser.userId", target = "updatedByUserId")
    @Mapping(source = "updatedByUser.fullName", target = "updatedByUserName")
    @Mapping(source = "rescheduledByUser.userId", target = "rescheduledByUserId")
    @Mapping(source = "rescheduledByUser.fullName", target = "rescheduledByUserName")
    @Mapping(target = "totalStudents", ignore = true)
    @Mapping(target = "approvedConsents", ignore = true)
    @Mapping(target = "declinedConsents", ignore = true)
    VaccinationCampaignResponseDto toDto(VaccinationCampaign entity);

    List<VaccinationCampaignResponseDto> toDtoList(List<VaccinationCampaign> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "campaignId", ignore = true)
    @Mapping(target = "organizedByUser", ignore = true)
    @Mapping(target = "consentForms", ignore = true)
    @Mapping(target = "vaccinations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "rescheduledAt", ignore = true)
    @Mapping(target = "rescheduledByUser", ignore = true)
    void updateEntityFromDto(CreateVaccinationCampaignRequestDto dto, @MappingTarget VaccinationCampaign entity);
}
