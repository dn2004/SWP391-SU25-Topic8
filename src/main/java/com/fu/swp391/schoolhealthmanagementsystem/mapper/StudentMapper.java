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
    @Mapping(target = "vaccinations", ignore = true) // Mới tạo, chưa có tiêm chủng
    @Mapping(target = "healthIncidents", ignore = true) // Mới tạo, chưa có sự cố sức khỏe
    @Mapping(source = "classGroup", target = "classGroup")
    @Mapping(source = "classValue", target = "classValue")
    Student createStudentRequestDtoToStudent(CreateStudentRequestDto dto);

    @Mapping(source = "className", target = "className")
    StudentDto studentToStudentDto(Student student);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "parentLinks", ignore = true)
    @Mapping(target = "invitationCode", ignore = true)
    @Mapping(target = "vaccinations", ignore = true)
    @Mapping(target = "healthIncidents", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(source = "classGroup", target = "classGroup")
    @Mapping(source = "classValue", target = "classValue")
    void updateStudentFromDto(UpdateStudentRequestDto dto, @MappingTarget Student student);
}