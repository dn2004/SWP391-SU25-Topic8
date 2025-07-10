package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.CreateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.UpdateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.validation.ClassNameValidator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.AfterMapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "id", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "status", constant = "ACTIVE") // Mặc định là active khi tạo mới
    @Mapping(target = "createdAt", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "updatedAt", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "parentLinks", ignore = true) // Mới tạo, chưa có link
    @Mapping(target = "invitationCode", ignore = true) // Sẽ được tạo ở service
    @Mapping(source = "classGroup", target = "classGroup")
    @Mapping(source = "classValue", target = "classValue")
    Student createStudentRequestDtoToStudent(CreateStudentRequestDto dto);

    @Mapping(source = "gender", target = "gender", qualifiedByName = "genderToDisplayNameString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToDisplayNameString")
    @Mapping(source = "classGroup", target = "classGroup", qualifiedByName = "classGroupToDisplayNameString")
    @Mapping(source = "className", target = "className") // Sử dụng phương thức getClassName()
    StudentDto studentToStudentDto(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parentLinks", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "vaccinations", ignore = true)
    @Mapping(target = "healthIncidents", ignore = true)
    @Mapping(source = "classGroup", target = "classGroup")
    @Mapping(source = "classValue", target = "classValue")
    void updateStudentFromDto(UpdateStudentRequestDto dto, @MappingTarget Student student);

    // This custom mapping method handles the Gender enum to its Vietnamese display name String conversion.
    @Named("genderToDisplayNameString")
    default String genderToDisplayNameString(Gender gender) {
        if (gender == null) {
            return null;
        }
        return gender.getDisplayName();
    }

    // This custom mapping method handles the StudentStatus enum to its display name String conversion.
    @Named("statusToDisplayNameString")
    default String statusToDisplayNameString(StudentStatus status) {
        if (status == null) {
            return null;
        }
        return status.getDisplayName();
    }

    // This custom mapping method handles the ClassGroup enum to its display name String conversion.
    @Named("classGroupToDisplayNameString")
    default String classGroupToDisplayNameString(ClassGroup classGroup) {
        if (classGroup == null) {
            return null;
        }
        return classGroup.getDisplayName();
    }
}