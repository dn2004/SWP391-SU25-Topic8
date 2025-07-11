package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.ChronicDiseaseStatusUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentChronicDiseaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
            description = """
                    ### Mô tả
                    Thêm một bản ghi bệnh mãn tính cho học sinh, có thể đính kèm file chứng nhận.
                    - **Phân quyền:**
                        - `Parent`: Thêm cho con mình, trạng thái mặc định là `PENDING`.
                        - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Thêm cho bất kỳ học sinh nào, trạng thái mặc định là `APPROVED`.
                    - **Thông báo:** Nếu phụ huynh thêm, hệ thống sẽ gửi thông báo đến nhân viên y tế để duyệt.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentChronicDiseaseResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
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
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của một bản ghi bệnh mãn tính.
                    - **Phân quyền:** 
                        - `Parent`: Chỉ xem được của con mình.
                        - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ ai.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentChronicDiseaseResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chronic-diseases/{chronicDiseaseId}")
    public ResponseEntity<StudentChronicDiseaseResponseDto> getChronicDiseaseById(
            @Parameter(description = "ID của bản ghi bệnh mãn tính") @PathVariable Long chronicDiseaseId) {
        log.info("API GET /api/chronic-diseases/{} được gọi", chronicDiseaseId);
        StudentChronicDiseaseResponseDto dto = chronicDiseaseService.getChronicDiseaseById(chronicDiseaseId);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Lấy danh sách tất cả bệnh mãn tính (phân trang, có bộ lọc)",
            description = """
                    ### Mô tả
                    Lấy danh sách tất cả các bản ghi bệnh mãn tính trong hệ thống.
                    - **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
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
            description = """
                    ### Mô tả
                    Lấy danh sách các bản ghi bệnh mãn tính của một học sinh cụ thể.
                    - **Phân quyền:** 
                        - `Parent`: Chỉ xem được của con mình.
                        - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ ai.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
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
            description = """
                    ### Mô tả
                    Cập nhật thông tin của một bản ghi bệnh mãn tính. File đính kèm mới sẽ thay thế file cũ.
                    - **Phân quyền:**
                        - `Parent`: Chỉ sửa được hồ sơ `PENDING` của con mình.
                        - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Không sửa được hồ sơ `PENDING` (phải duyệt), chỉ sửa được các hồ sơ đã được duyệt.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentChronicDiseaseResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi", content = @Content)
    })
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
            description = """
                    ### Mô tả
                    Duyệt một hồ sơ bệnh mãn tính đang ở trạng thái `PENDING`.
                    - **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
                    - **Thông báo:** Gửi thông báo đến phụ huynh về kết quả duyệt.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Duyệt thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentChronicDiseaseResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi", content = @Content)
    })
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
            description = """
                    ### Mô tả
                    Xóa một bản ghi bệnh mãn tính.
                    - **Phân quyền:**
                        - `Parent`: Chỉ xóa được hồ sơ `PENDING` của con mình.
                        - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xóa bất kỳ hồ sơ nào.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/chronic-diseases/{chronicDiseaseId}")
    public ResponseEntity<Void> deleteChronicDisease(
            @Parameter(description = "ID của bản ghi bệnh mãn tính cần xóa") @PathVariable Long chronicDiseaseId) {
        log.info("API DELETE /api/chronic-diseases/{} được gọi", chronicDiseaseId);
        chronicDiseaseService.deleteChronicDiseaseForCurrentUser(chronicDiseaseId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy URL truy cập (đã ký) cho file đính kèm",
            description = """
                    ### Mô tả
                    Nhận một URL tạm thời (đã ký) để truy cập file đính kèm của một bản ghi bệnh mãn tính.
                    - **Phân quyền:** Người dùng đã xác thực và có quyền xem bản ghi tương ứng.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy URL thành công",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(example = "{\"url\": \"SIGNED_URL\"}"))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy file đính kèm", content = @Content)
    })
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
