package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.VaccinationStatusUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface StudentVaccinationMapper {

    @Mapping(source = "student.studentId", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentFullName")
    @Mapping(source = "student.className", target = "studentClassName")
    @Mapping(source = "approvedByUser.userId", target = "approvedByUserId")
    @Mapping(source = "approvedByUser.fullName", target = "approvedByUserFullName")
    @Mapping(target = "hasProofFile", ignore = true)
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
    StudentVaccination requestDtoToEntity(StudentVaccinationRequestDto dto);

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
    void updateEntityFromRequestDto(StudentVaccinationRequestDto dto, @MappingTarget StudentVaccination targetEntity);

    @Mapping(source = "originalFilename", target = "proofFileOriginalName")
    @Mapping(target = "proofFileType", ignore = true)
    @Mapping(source = "publicId", target = "proofPublicId")
    @Mapping(source = "resourceType", target = "proofResourceType")
    void updateProofFileDetailsFromUploadResult(CloudinaryUploadResponse uploadResult, @MappingTarget StudentVaccination targetEntity);

    default void clearProofFileDetails(@MappingTarget StudentVaccination entity) {
        entity.setProofFileOriginalName(null);
        entity.setProofFileType(null);
        entity.setProofPublicId(null);
        entity.setProofResourceType(null);
    }

    default void clearApprovedByUser(@MappingTarget StudentVaccination entity) {
        entity.setApprovedByUser(null);
        entity.setApprovedAt(null);
        entity.setApproverNotes(null);
    }
}