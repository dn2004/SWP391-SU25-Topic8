package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.parent.LinkStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.ParentStudentLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
@Tag(name = "Parent", description = "APIs cho Phụ huynh")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('Parent')")
@Slf4j
public class ParentController {

    private final ParentStudentLinkService parentStudentLinkService;

    @PostMapping("/link-student")
    @Operation(summary = "Phụ huynh liên kết với học sinh bằng mã mời",
            description = """
### Mô tả
Cho phép phụ huynh liên kết tài khoản của mình với hồ sơ của một học sinh bằng cách sử dụng mã mời và ngày sinh của học sinh.
- **Phân quyền:** Yêu cầu vai trò `Parent`.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liên kết thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mã mời hoặc học sinh", content = @Content),
        @ApiResponse(responseCode = "409", description = "Đã liên kết với học sinh này", content = @Content)
    })
    public ResponseEntity<String> linkStudent(@Valid @RequestBody LinkStudentRequestDto requestDto) {
        parentStudentLinkService.linkParentToStudentByInvitation(requestDto);
        return ResponseEntity.ok().body("Liên kết thành công với học sinh.");
    }

    @GetMapping("/my-students")
    @Operation(
            summary = "Lấy danh sách các học sinh đã liên kết của phụ huynh hiện tại (phân trang)",
            description = """
### Mô tả
Trả về danh sách các học sinh đã được liên kết với tài khoản phụ huynh đang đăng nhập.
- **Phân quyền:** Yêu cầu vai trò `Parent`.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<Page<StudentDto>> getMyLinkedStudents(
            @RequestParam(required = false) StudentStatus status,
            @ParameterObject
            @PageableDefault(size = 5, sort = "student.fullName")
            Pageable pageable) {
        log.info("API Phụ huynh: Yêu cầu lấy danh sách học sinh đã liên kết với trạng thái {}", status);
        Page<StudentDto> linkedStudents = parentStudentLinkService.getMyLinkedStudents(status, pageable);
        return ResponseEntity.ok(linkedStudents);
    }
}