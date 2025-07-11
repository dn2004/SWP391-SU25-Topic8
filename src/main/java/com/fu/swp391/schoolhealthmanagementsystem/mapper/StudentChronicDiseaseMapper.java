package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentChronicDisease;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface StudentChronicDiseaseMapper {

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.fullName", target = "studentFullName")
    @Mapping(source = "student.className", target = "studentClassName")
    @Mapping(source = "createdByUser.userId", target = "createdByUserId")
    @Mapping(source = "createdByUser.fullName", target = "createdByUserFullName")
    @Mapping(source = "updatedByUser.userId", target = "updatedByUserId")
    @Mapping(source = "updatedByUser.fullName", target = "updatedByUserFullName")
    @Mapping(source = "approvedByUser.userId", target = "approvedByUserId")
    @Mapping(source = "approvedByUser.fullName", target = "approvedByUserFullName")
    @Mapping(target = "hasAttachmentFile", expression = "java(entity.getAttachmentPublicId() != null && !entity.getAttachmentPublicId().isBlank())")
    StudentChronicDiseaseResponseDto toDto(StudentChronicDisease entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", source = "student")
    @Mapping(target = "createdByUser", ignore = true) // Sẽ được set ở service
    @Mapping(target = "updatedByUser", ignore = true) // Sẽ được set ở service
    @Mapping(target = "approvedByUser", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approverNotes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "attachmentFileOriginalName", ignore = true)
    @Mapping(target = "attachmentFileType", ignore = true)
    @Mapping(target = "attachmentPublicId", ignore = true)
    @Mapping(target = "attachmentResourceType", ignore = true)
    @Mapping(target = "status", ignore = true) // Sẽ được set ở service
    StudentChronicDisease toEntity(StudentChronicDiseaseRequestDto dto, Student student);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "diseaseName", ignore = true)
    @Mapping(target = "diagnosedDate", ignore = true)
    @Mapping(target = "diagnosingDoctor", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "attachmentFileOriginalName", ignore = true)
    @Mapping(target = "attachmentFileType", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdByUser", ignore = true)
    @Mapping(target = "updatedByUser", ignore = true)
    @Mapping(target = "approvedByUser", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "approverNotes", ignore = true)
    void updateEntityFromDto(StudentChronicDiseaseRequestDto dto, @MappingTarget StudentChronicDisease entity);

    @Mapping(target = "attachmentPublicId", source = "publicId")
    @Mapping(target = "attachmentResourceType", source = "resourceType")
    void updateAttachmentFileDetailsFromUploadResult(CloudinaryUploadResponse uploadResult, @MappingTarget StudentChronicDisease entity);
}
