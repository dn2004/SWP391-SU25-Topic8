package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination.StudentVaccinationUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StudentVaccinationMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentFullName")
    @Mapping(source = "student.className", target = "studentClassName")
    @Mapping(source = "approvedByUser.userId", target = "approvedByUserId")
    @Mapping(source = "approvedByUser.fullName", target = "approvedByUserFullName")
    @Mapping(source = "createdByUser.userId", target = "createdByUserId")
    @Mapping(source = "createdByUser.fullName", target = "createdByUserFullName")
    @Mapping(source = "updatedByUser.userId", target = "updatedByUserId")
    @Mapping(source = "updatedByUser.fullName", target = "updatedByUserFullName")
    @Mapping(target = "hasProofFile", expression = "java(entity.getProofPublicId() != null && !entity.getProofPublicId().isBlank())")
    StudentVaccinationResponseDto toDto(StudentVaccination entity);


    @Mapping(target = "studentVaccinationId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "proofFileOriginalName", ignore = true)
    @Mapping(target = "proofFileType", ignore = true)
    @Mapping(target = "proofPublicId", ignore = true)
    @Mapping(target = "proofResourceType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedByUser", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approverNotes", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    StudentVaccination requestDtoToEntity(StudentVaccinationRequestDto dto);

    default void updateEntityFromDto(StudentVaccinationUpdateRequestDto dto, @MappingTarget StudentVaccination entity) {
        if (dto == null) {
            return;
        }

        if (dto.vaccineName() != null && !dto.vaccineName().isBlank()) {
            entity.setVaccineName(dto.vaccineName());
        }
        if (dto.provider() != null && !dto.provider().isBlank()) {
            entity.setProvider(dto.provider());
        }
        if (dto.notes() != null && !dto.notes().isBlank()) {
            entity.setNotes(dto.notes());
        }
        if (dto.vaccinationDate() != null) {
            entity.setVaccinationDate(dto.vaccinationDate());
        }
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "originalFilename", target = "proofFileOriginalName")
    @Mapping(source = "format", target = "proofFileType")
    @Mapping(source = "publicId", target = "proofPublicId")
    @Mapping(source = "resourceType", target = "proofResourceType")
    @Mapping(target = "studentVaccinationId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "vaccineName", ignore = true)
    @Mapping(target = "vaccinationDate", ignore = true)
    @Mapping(target = "provider", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "approvedByUser", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approverNotes", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    void updateProofFileDetailsFromUploadResult(CloudinaryUploadResponse uploadResult, @MappingTarget StudentVaccination targetEntity);

    default void clearProofFileDetails(@MappingTarget StudentVaccination entity) {
        entity.setProofFileOriginalName(null);
        entity.setProofFileType(null);
        entity.setProofPublicId(null);
        entity.setProofResourceType(null);
    }
}
