package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.CreateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto; // Bạn cần tạo DTO này
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "id", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "status", constant = "ACTIVE") // Mặc định là active khi tạo mới
    @Mapping(target = "createdAt", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "updatedAt", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "parentLinks", ignore = true) // Mới tạo, chưa có link
    @Mapping(target = "invitationCode", ignore = true) // Sẽ được tạo ở service
    Student createStudentRequestDtoToStudent(CreateStudentRequestDto dto);

    @Mapping(source = "gender", target = "gender", qualifiedByName = "genderToDisplayNameString")
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToDisplayNameString")
    StudentDto studentToStudentDto(Student student);

    // This custom mapping method handles the Gender enum to its Vietnamese display name String conversion.
    @Named("genderToDisplayNameString")
    default String genderToDisplayNameString(Gender gender) {
        if (gender == null) {
            return null;
        }
        return gender.getDisplayName(); // Use the getDisplayName() method
    }

    // This custom mapping method handles the StudentStatus enum to its display name String conversion.
    @Named("statusToDisplayNameString")
    default String statusToDisplayNameString(StudentStatus status) {
        if (status == null) {
            return null;
        }
        return status.getDisplayName();
    }
}