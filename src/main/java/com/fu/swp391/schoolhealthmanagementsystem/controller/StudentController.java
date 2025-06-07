package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students") // Hoặc một base path chung hơn
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "APIs quản lý học sinh")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StudentController {

    private final StudentService studentService;


    @GetMapping("/{studentId}")
    @Operation(summary = "Lấy thông tin chi tiết một học sinh bằng ID")
    // Cho phép Admin, Staff, và Parent (service sẽ kiểm tra parent có link không)
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff', 'Parent')")
    public ResponseEntity<StudentDto> getStudentById(
            @Parameter(description = "ID của học sinh cần truy vấn") @PathVariable Long studentId) {
        log.info("API: Yêu cầu lấy thông tin học sinh ID: {}", studentId);
        StudentDto studentDto = studentService.getStudentById(studentId);
        return ResponseEntity.ok(studentDto);
    }
    @GetMapping
    @Operation(summary = "Lấy danh sách học sinh (phân trang)")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')") // Chỉ cho phép các vai trò này
    public ResponseEntity<Page<StudentDto>> getAllStudents(
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {
        log.info("API: Yêu cầu lấy danh sách học sinh - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<StudentDto> studentPage = studentService.getAllStudents(pageable);
        return ResponseEntity.ok(studentPage);
    }
}