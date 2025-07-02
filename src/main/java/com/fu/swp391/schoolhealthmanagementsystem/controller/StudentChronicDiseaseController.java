package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.ChronicDiseaseStatusUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentChronicDiseaseRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentChronicDiseaseResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
 import com.fu.swp391.schoolhealthmanagementsystem.service.StudentChronicDiseaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Student Chronic Disease Management", description = "API để quản lý bệnh mãn tính của học sinh")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StudentChronicDiseaseController {

    private final StudentChronicDiseaseService chronicDiseaseService;

    @Operation(summary = "Thêm mới một bản ghi bệnh mãn tính cho học sinh",
            description = "Người dùng đã xác thực có thể thêm. Phụ huynh thêm sẽ ở trạng thái PENDING, nhân viên thêm sẽ tự động APPROVE.")
    @ApiResponse(responseCode = "201", description = "Tạo thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentChronicDiseaseResponseDto.class)))
    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/students/{studentId}/chronic-diseases", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<StudentChronicDiseaseResponseDto> addChronicDisease(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @Valid @ModelAttribute StudentChronicDiseaseRequestDto dto) {
        log.info("API POST /api/students/{}/chronic-diseases được gọi", studentId);
        StudentChronicDiseaseResponseDto createdDto = chronicDiseaseService.addChronicDisease(studentId, dto);
        return new ResponseEntity<>(createdDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin bệnh mãn tính theo ID",
            description = "Người dùng đã xác thực có thể lấy thông tin. Service sẽ kiểm tra quyền truy cập chi tiết.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chronic-diseases/{chronicDiseaseId}")
    public ResponseEntity<StudentChronicDiseaseResponseDto> getChronicDiseaseById(
            @Parameter(description = "ID của bản ghi bệnh mãn tính") @PathVariable Long chronicDiseaseId) {
        log.info("API GET /api/chronic-diseases/{} được gọi", chronicDiseaseId);
        StudentChronicDiseaseResponseDto dto = chronicDiseaseService.getChronicDiseaseById(chronicDiseaseId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Lấy danh sách tất cả bệnh mãn tính (phân trang, có bộ lọc)",
            description = "Chỉ Nhân viên y tế, Quản lý hoặc Admin có thể truy cập.")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    @GetMapping("/chronic-diseases")
    public ResponseEntity<Page<StudentChronicDiseaseResponseDto>> getAllChronicDiseases(
            @Parameter(description = "Lọc theo tên học sinh") @RequestParam(required = false) String studentName,
            @Parameter(description = "Lọc theo tên bệnh") @RequestParam(required = false) String diseaseName,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) StudentChronicDiseaseStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API GET /api/chronic-diseases (phân trang) được gọi");
        Page<StudentChronicDiseaseResponseDto> page = chronicDiseaseService.getAllChronicDiseases(studentName, diseaseName, status, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Lấy danh sách bệnh mãn tính của một học sinh (phân trang)",
            description = "Người dùng đã xác thực có thể lấy thông tin. Service sẽ kiểm tra quyền truy cập chi tiết.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/students/{studentId}/chronic-diseases")
    public ResponseEntity<Page<StudentChronicDiseaseResponseDto>> getAllChronicDiseasesByStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @Parameter(description = "Lọc theo tên bệnh") @RequestParam(required = false) String diseaseName,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) StudentChronicDiseaseStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API GET /api/students/{}/chronic-diseases (phân trang) được gọi", studentId);
        Page<StudentChronicDiseaseResponseDto> page = chronicDiseaseService.getAllChronicDiseasesByStudentId(studentId, diseaseName, status, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "Cập nhật thông tin bệnh mãn tính",
            description = "Người dùng đã xác thực có thể cập nhật. Phụ huynh chỉ sửa được hồ sơ PENDING. Nhân viên không sửa được hồ sơ PENDING (phải duyệt). File đính kèm mới sẽ thay thế file cũ.")
    @PreAuthorize("isAuthenticated()")
    @PutMapping(value = "/chronic-diseases/{chronicDiseaseId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<StudentChronicDiseaseResponseDto> updateChronicDisease(
            @Parameter(description = "ID của bản ghi bệnh mãn tính cần cập nhật") @PathVariable Long chronicDiseaseId,
            @Valid @ModelAttribute StudentChronicDiseaseRequestDto dto) {
        log.info("API PUT /api/chronic-diseases/{} được gọi", chronicDiseaseId);
        StudentChronicDiseaseResponseDto updatedDto = chronicDiseaseService.updateChronicDiseaseForCurrentUser(chronicDiseaseId, dto);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Duyệt (chấp nhận/từ chối) một hồ sơ bệnh mãn tính",
            description = "Chỉ Nhân viên y tế, Quản lý hoặc Admin có thể duyệt các hồ sơ đang ở trạng thái PENDING.")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    @PatchMapping("/chronic-diseases/{chronicDiseaseId}/mediate")
    public ResponseEntity<StudentChronicDiseaseResponseDto> mediateChronicDiseaseStatus(
            @Parameter(description = "ID của bản ghi bệnh mãn tính") @PathVariable Long chronicDiseaseId,
            @Valid @RequestBody ChronicDiseaseStatusUpdateRequestDto dto) {
        log.info("API PATCH /api/chronic-diseases/{}/mediate được gọi với trạng thái mới: {}", chronicDiseaseId, dto.newStatus());
        StudentChronicDiseaseResponseDto updatedDto = chronicDiseaseService.mediateChronicDiseaseStatus(chronicDiseaseId, dto);
        return ResponseEntity.ok(updatedDto);
    }

    @Operation(summary = "Xóa một bản ghi bệnh mãn tính",
            description = "Người dùng đã xác thực có thể xóa. Phụ huynh chỉ xóa được hồ sơ PENDING. Nhân viên có thể xóa bất kỳ hồ sơ nào.")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/chronic-diseases/{chronicDiseaseId}")
    public ResponseEntity<Void> deleteChronicDisease(
            @Parameter(description = "ID của bản ghi bệnh mãn tính cần xóa") @PathVariable Long chronicDiseaseId) {
        log.info("API DELETE /api/chronic-diseases/{} được gọi", chronicDiseaseId);
        chronicDiseaseService.deleteChronicDiseaseForCurrentUser(chronicDiseaseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy URL truy cập (đã ký) cho file đính kèm",
            description = "Người dùng đã xác thực và có quyền sẽ nhận được một URL tạm thời để truy cập file.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chronic-diseases/{chronicDiseaseId}/file-access-url")
    public ResponseEntity<Map<String, String>> getAttachmentAccessUrl(
            @Parameter(description = "ID của bản ghi bệnh mãn tính") @PathVariable Long chronicDiseaseId) {
        log.info("API GET /api/chronic-diseases/{}/file-access-url được gọi", chronicDiseaseId);
        String signedUrl = chronicDiseaseService.getSignedUrlForAttachment(chronicDiseaseId);
        if (signedUrl == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy file đính kèm hoặc lỗi tạo URL."));
        }
        return ResponseEntity.ok(Map.of("url", signedUrl));
    }
}
