package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring") // componentModel = "spring" để tự động tạo Spring bean
public interface StudentVaccinationMapper {
    @Mapping(source = "student", target = "studentId", qualifiedByName = "studentToStudentId")

    StudentVaccinationResponseDto toDto(StudentVaccination entity);

    @Named("studentToStudentId")
    default Long studentToStudentId(Student student) {
        return student != null ? student.getStudentId() : null;
    }

    @Mapping(target = "studentVaccinationId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // Ignore các trường file vì sẽ được cập nhật bởi phương thức khác
    @Mapping(target = "proofFileOriginalName", ignore = true)
    @Mapping(target = "proofFileUrl", ignore = true)
    @Mapping(target = "proofFileType", ignore = true)
    @Mapping(target = "proofPublicId", ignore = true)
    @Mapping(target = "proofResourceType", ignore = true)
    StudentVaccination requestDtoToEntity(StudentVaccinationRequestDto dto);

    // Phương thức mới để cập nhật thông tin file từ CloudinaryUploadResponse vào Entity
    // Nó sẽ chỉ cập nhật các trường file
    @Mapping(source = "originalFilename", target = "proofFileOriginalName")
    @Mapping(source = "url", target = "proofFileUrl")
    @Mapping(source = "contentType", target = "proofFileType") // Sử dụng contentType từ uploadResult
    @Mapping(source = "publicId", target = "proofPublicId")
    @Mapping(source = "resourceType", target = "proofResourceType")
    void updateProofFileDetailsFromUploadResult(CloudinaryUploadResponse uploadResult, @MappingTarget StudentVaccination targetEntity);


    // Phương thức update chính có thể gọi updateProofFileDetailsFromUploadResult
    @Mapping(target = "studentVaccinationId", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // Ignore các trường file vì sẽ được cập nhật riêng nếu có file mới
    @Mapping(target = "proofFileOriginalName", ignore = true)
    @Mapping(target = "proofFileUrl", ignore = true)
    @Mapping(target = "proofFileType", ignore = true)
    @Mapping(target = "proofPublicId", ignore = true)
    @Mapping(target = "proofResourceType", ignore = true)
    void updateEntityFromRequestDto(StudentVaccinationRequestDto dto, @MappingTarget StudentVaccination targetEntity);


}