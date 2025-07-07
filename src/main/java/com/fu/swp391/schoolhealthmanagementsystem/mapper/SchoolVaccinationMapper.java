package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.RecordVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.SchoolVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SchoolVaccination;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PostVaccinationMonitoringMapper.class})
public interface SchoolVaccinationMapper {

    @Mapping(source = "campaign.campaignId", target = "campaignId")
    @Mapping(source = "campaign.campaignName", target = "campaignName")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentName")
    @Mapping(source = "student.className", target = "studentClass")
    @Mapping(source = "consent.consentId", target = "consentId")
    @Mapping(source = "administeredByUser.userId", target = "administeredByUserId")
    @Mapping(source = "administeredByUser.fullName", target = "administeredByUserName")
    SchoolVaccinationResponseDto toDto(SchoolVaccination entity);

    List<SchoolVaccinationResponseDto> toDtoList(List<SchoolVaccination> entities);

    @Mapping(target = "schoolVaccinationId", ignore = true)
    @Mapping(target = "campaign", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "consent", ignore = true)
    @Mapping(target = "vaccinationDate", ignore = true)
    @Mapping(target = "administeredByUser", ignore = true)
    @Mapping(target = "monitoringRecord", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(RecordVaccinationRequestDto dto, @MappingTarget SchoolVaccination entity);
}
