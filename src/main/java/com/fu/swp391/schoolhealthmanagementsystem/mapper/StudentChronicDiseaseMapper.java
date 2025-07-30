package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseUpdateRequestDto;
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

    // Thay thế logic tự động bằng logic tùy chỉnh để tránh ghi đè bằng chuỗi rỗng
    default void updateEntityFromDto(StudentChronicDiseaseUpdateRequestDto dto, @MappingTarget StudentChronicDisease entity) {
        if (dto == null) {
            return;
        }

        // Chỉ cập nhật nếu giá trị mới không rỗng/trắng
        if (dto.diseaseName() != null && !dto.diseaseName().isBlank()) {
            entity.setDiseaseName(dto.diseaseName());
        }
        if (dto.diagnosingDoctor() != null && !dto.diagnosingDoctor().isBlank()) {
            entity.setDiagnosingDoctor(dto.diagnosingDoctor());
        }
        if (dto.notes() != null && !dto.notes().isBlank()) {
            entity.setNotes(dto.notes());
        }

        // Đối với các kiểu không phải String, chỉ cần kiểm tra null
        if (dto.diagnosedDate() != null) {
            entity.setDiagnosedDate(dto.diagnosedDate());
        }
    }

    @Mapping(target = "attachmentPublicId", source = "publicId")
    @Mapping(target = "attachmentResourceType", source = "resourceType")
    void updateAttachmentFileDetailsFromUploadResult(CloudinaryUploadResponse uploadResult, @MappingTarget StudentChronicDisease entity);
}
