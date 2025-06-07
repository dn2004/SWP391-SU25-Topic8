package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.parent.LinkStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.ParentStudentLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@Tag(name = "Parent", description = "APIs cho Phụ huynh")
@SecurityRequirement(name = "parent-api")
@PreAuthorize("hasRole('Parent')") // Requires Parent role
@Slf4j
public class ParentController {

    private final ParentStudentLinkService parentStudentLinkService;

    @PostMapping("/link-student")
    @Operation(summary = "Phụ huynh liên kết với học sinh bằng mã mời")
    public ResponseEntity<String> linkStudent(@Valid @RequestBody LinkStudentRequestDto requestDto) {
        log.info("API Phụ huynh: Yêu cầu liên kết với học sinh bằng mã mời: {}", requestDto.invitationCode());
        parentStudentLinkService.linkParentToStudentByInvitation(requestDto);
        return ResponseEntity.ok("Liên kết với học sinh thành công.");
    }

    @GetMapping("/my-students")
    @Operation(summary = "Lấy danh sách các học sinh đã liên kết của phụ huynh hiện tại (phân trang)")
    public ResponseEntity<Page<StudentDto>> getMyLinkedStudents(
            @PageableDefault(size = 5, sort = "student.fullName") Pageable pageable) {
        log.info("API Phụ huynh: Yêu cầu lấy danh sách học sinh đã liên kết.");
        Page<StudentDto> linkedStudents = parentStudentLinkService.getMyLinkedStudents(pageable); // THAY ĐỔI Ở ĐÂY: Gọi từ parentService
        return ResponseEntity.ok(linkedStudents);
    }
}