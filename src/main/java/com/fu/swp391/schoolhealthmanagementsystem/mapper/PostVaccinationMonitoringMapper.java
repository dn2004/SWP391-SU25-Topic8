package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.CreatePostVaccinationMonitoringRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.PostVaccinationMonitoringResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.PostVaccinationMonitoring;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PostVaccinationMonitoringMapper {

    @Mapping(target = "monitoringId", ignore = true)
    @Mapping(target = "schoolVaccination", ignore = true)
    @Mapping(target = "monitoringTime", ignore = true)
    @Mapping(target = "recordedByUser", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    PostVaccinationMonitoring toEntity(CreatePostVaccinationMonitoringRequestDto dto);

    @Mapping(source = "schoolVaccination.schoolVaccinationId", target = "schoolVaccinationId")
    @Mapping(source = "recordedByUser.userId", target = "recordedByUserId")
    @Mapping(source = "recordedByUser.fullName", target = "recordedByUserName")
    PostVaccinationMonitoringResponseDto toDto(PostVaccinationMonitoring entity);
}
