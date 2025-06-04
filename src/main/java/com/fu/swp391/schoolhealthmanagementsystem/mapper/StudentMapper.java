package com.fu.swp391.schoolhealthmanagementsystem.mapper;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.CreateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto; // Bạn cần tạo DTO này
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "studentId", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "active", constant = "true") // Mặc định là active khi tạo mới
    @Mapping(target = "createdAt", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "updatedAt", ignore = true) // Sẽ được tự sinh
    @Mapping(target = "parentLinks", ignore = true) // Mới tạo, chưa có link
    @Mapping(target = "invitationCode", ignore = true) // Sẽ được tạo ở service
    Student createStudentRequestDtoToStudent(CreateStudentRequestDto dto);

    StudentDto studentToStudentDto(Student student);
}