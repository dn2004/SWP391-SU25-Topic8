package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.ErrorResponseDto; // Giả sử bạn có DTO này
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.VaccinationStatusUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentVaccinationService;
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

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Student Vaccination Management", description = "API để quản lý thông tin tiêm chủng của học sinh")
@SecurityRequirement(name = "bearerAuth") // Áp dụng cho tất cả API trong controller này
@Slf4j
public class StudentVaccinationController {

    private final StudentVaccinationService vaccinationService;

    @Operation(summary = "Thêm mới thông tin tiêm chủng cho học sinh",
            description = "Phụ huynh, Quản trị viên hoặc Nhân viên y tế có thể thêm. Trạng thái mặc định sẽ là PENDING.")
    @ApiResponse(responseCode = "201", description = "Tạo thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StudentVaccinationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
            content = @Content())
    @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện hành động này",
            content = @Content())
    @ApiResponse(responseCode = "404", description = "Học sinh không tìm thấy",
            content = @Content())
    @PreAuthorize("hasAnyRole('Parent', 'SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @PostMapping(path = "/students/{studentId}/vaccinations", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<StudentVaccinationResponseDto> addVaccinationToStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @Valid @ModelAttribute StudentVaccinationRequestDto vaccinationDto) {
        log.info("API POST /api/students/{}/vaccinations được gọi", studentId);
        StudentVaccinationResponseDto createdVaccinationDto = vaccinationService.addVaccination(studentId, vaccinationDto);
        return new ResponseEntity<>(createdVaccinationDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin tiêm chủng theo ID bản ghi tiêm chủng",
            description = "Người dùng đã xác thực có thể lấy thông tin. Service sẽ kiểm tra quyền truy cập chi tiết.")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentVaccinationResponseDto.class)))
    @ApiResponse(responseCode = "403", description = "Không có quyền truy cập bản ghi này",
            content = @Content())
    @ApiResponse(responseCode = "404", description = "Thông tin tiêm chủng hoặc học sinh không tìm thấy",
            content = @Content())
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vaccinations/{vaccinationId}")
    public ResponseEntity<StudentVaccinationResponseDto> getVaccinationById(
            @Parameter(description = "ID của bản ghi tiêm chủng") @PathVariable Long vaccinationId) {
        log.info("API GET /api/vaccinations/{} được gọi", vaccinationId);
        // Service sẽ tự lấy studentId từ vaccinationId và kiểm tra quyền
        StudentVaccinationResponseDto vaccinationDto =
                vaccinationService.getVaccinationResponseByIdForCurrentUser(vaccinationId);
        return ResponseEntity.ok(vaccinationDto);
    }

    @Operation(summary = "Cập nhật thông tin tiêm chủng cho học sinh",
            description = "Phụ huynh (nếu bản ghi đang PENDING), Quản trị viên hoặc Nhân viên y tế có thể cập nhật. " +
                    "File bằng chứng mới (nếu có) sẽ thay thế file cũ. " +
                    "Nếu bản ghi không ở trạng thái PENDING và được cập nhật, trạng thái sẽ reset về PENDING.")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentVaccinationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ",
            content = @Content())
    @ApiResponse(responseCode = "403", description = "Không có quyền cập nhật bản ghi này",
            content = @Content())
    @ApiResponse(responseCode = "404", description = "Bản ghi tiêm chủng không tìm thấy",
            content = @Content())
    @PreAuthorize("hasAnyRole('Parent', 'SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @PutMapping(value = "/vaccinations/{vaccinationId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<StudentVaccinationResponseDto> updateVaccination(
            @Parameter(description = "ID của bản ghi tiêm chủng cần cập nhật") @PathVariable Long vaccinationId,
            @Valid @ModelAttribute StudentVaccinationRequestDto vaccinationDto) {
        log.info("API PUT /api/vaccinations/{} được gọi", vaccinationId);
        StudentVaccinationResponseDto updatedVaccinationDto =
                vaccinationService.updateVaccinationForCurrentUser(vaccinationId, vaccinationDto);
        return ResponseEntity.ok(updatedVaccinationDto);
    }

    @Operation(summary = "Duyệt/Thay đổi trạng thái bản ghi tiêm chủng",
            description = "Chỉ Quản trị viên hoặc Nhân viên y tế có thể thực hiện. " +
                    "Chỉ cho phép chuyển từ trạng thái PENDING sang APPROVE hoặc REJECTED.")
    @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentVaccinationResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "Dữ liệu trạng thái không hợp lệ hoặc hành động không được phép",
            content = @Content())
    @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện hành động này",
            content = @Content())
    @ApiResponse(responseCode = "404", description = "Bản ghi tiêm chủng không tìm thấy",
            content = @Content())
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @PatchMapping("/vaccinations/{vaccinationId}/status")
    public ResponseEntity<StudentVaccinationResponseDto> mediateVaccinationStatus(
            @Parameter(description = "ID của bản ghi tiêm chủng cần cập nhật trạng thái") @PathVariable Long vaccinationId,
            @Valid @RequestBody VaccinationStatusUpdateRequestDto statusUpdateRequestDto) {
        log.info("API PATCH /api/vaccinations/{}/status được gọi với trạng thái mới: {}", vaccinationId, statusUpdateRequestDto.newStatus());
        StudentVaccinationResponseDto mediatedVaccinationDto =
                vaccinationService.mediateVaccinationStatusForCurrentUser(vaccinationId, statusUpdateRequestDto);
        return ResponseEntity.ok(mediatedVaccinationDto);
    }

    @Operation(summary = "Xóa thông tin tiêm chủng",
            description = "Phụ huynh (nếu bản ghi đang PENDING) hoặc Quản trị viên có thể xóa.")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    @ApiResponse(responseCode = "403", description = "Không có quyền xóa bản ghi này",
            content = @Content())
    @ApiResponse(responseCode = "404", description = "Thông tin tiêm chủng không tìm thấy",
            content = @Content())
    @PreAuthorize("hasAnyRole('Parent', 'SchoolAdmin')") // Chỉ Parent hoặc SchoolAdmin được gọi API này
    @DeleteMapping("/vaccinations/{vaccinationId}")
    public ResponseEntity<Void> deleteVaccination(
            @Parameter(description = "ID của bản ghi tiêm chủng cần xóa") @PathVariable Long vaccinationId) {
        log.info("API DELETE /api/vaccinations/{} được gọi", vaccinationId);
        vaccinationService.deleteVaccinationForCurrentUser(vaccinationId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lấy danh sách tất cả thông tin tiêm chủng của một học sinh (phân trang)",
            description = "Trả về đối tượng Page chứa danh sách các bản ghi tiêm chủng của học sinh. " +
                    "Hỗ trợ lọc theo tên vắc-xin, khoảng ngày tiêm và trạng thái. " +
                    "Hỗ trợ các tham số query: `page`, `size`, `sort`.")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Page.class))) // Response là một Page của StudentVaccinationResponseDto
    @ApiResponse(responseCode = "403", description = "Không có quyền xem danh sách này",
            content = @Content())
    @ApiResponse(responseCode = "404", description = "Học sinh không tìm thấy",
            content = @Content())
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/students/{studentId}/vaccinations")
    public ResponseEntity<Page<StudentVaccinationResponseDto>> getAllVaccinationsByStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @Parameter(description = "Lọc theo tên vắc-xin (không phân biệt chữ hoa chữ thường)")
            @RequestParam(required = false) String vaccineName,
            @Parameter(description = "Lọc từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Lọc đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Lọc theo trạng thái")
            @RequestParam(required = false) StudentVaccinationStatus status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "vaccinationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("API GET /api/students/{}/vaccinations (phân trang) được gọi với pageable: {}", studentId, pageable);
        Page<StudentVaccinationResponseDto> vaccinationsPage =
                vaccinationService.getAllVaccinationsByStudentIdPage(studentId, vaccineName, fromDate, toDate, status, pageable);
        return ResponseEntity.ok(vaccinationsPage);
    }

    @Operation(summary = "Lấy danh sách tất cả thông tin tiêm chủng (phân trang, có bộ lọc)",
            description = "Chỉ Quản trị viên, Nhân viên y tế hoặc Quản lý nhân viên có thể truy cập. " +
                    "Trả về đối tượng Page chứa danh sách các bản ghi tiêm chủng. " +
                    "Hỗ trợ lọc theo tên học sinh, tên vắc-xin, khoảng ngày tiêm và trạng thái. " +
                    "Hỗ trợ các tham số query: `page`, `size`, `sort`.")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "403", description = "Không có quyền xem danh sách này",
            content = @Content())
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @GetMapping("/vaccinations")
    public ResponseEntity<Page<StudentVaccinationResponseDto>> getAllVaccinations(
            @Parameter(description = "Lọc theo tên học sinh (không phân biệt chữ hoa chữ thường)")
            @RequestParam(required = false) String studentName,
            @Parameter(description = "Lọc theo tên vắc-xin (không phân biệt chữ hoa chữ thường)")
            @RequestParam(required = false) String vaccineName,
            @Parameter(description = "Lọc từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Lọc đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Lọc theo trạng thái")
            @RequestParam(required = false) StudentVaccinationStatus status,
            @ParameterObject
            @PageableDefault(size = 10, sort = "vaccinationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("API GET /api/vaccinations (phân trang) được gọi với các bộ lọc và pageable: {}", pageable);
        Page<StudentVaccinationResponseDto> vaccinationsPage =
                vaccinationService.getAllVaccinations(studentName, vaccineName, fromDate, toDate, status, pageable);
        return ResponseEntity.ok(vaccinationsPage);
    }

    @Operation(summary = "Lấy danh sách các bản ghi tiêm chủng đang chờ duyệt",
            description = "Chỉ Quản trị viên, Nhân viên y tế hoặc Quản lý nhân viên có thể truy cập. " +
                    "Trả về danh sách các bản ghi có trạng thái 'PENDING', được sắp xếp theo ngày tạo. " +
                    "Hỗ trợ các tham số query: `page`, `size`, `sort`.")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Page.class)))
    @ApiResponse(responseCode = "403", description = "Không có quyền xem danh sách này",
            content = @Content())
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @GetMapping("/vaccinations/pending")
    public ResponseEntity<Page<StudentVaccinationResponseDto>> getPendingVaccinations(
            @ParameterObject
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("API GET /api/vaccinations/pending (phân trang) được gọi với pageable: {}", pageable);
        Page<StudentVaccinationResponseDto> pendingVaccinationsPage =
                vaccinationService.getPendingVaccinations(pageable);
        return ResponseEntity.ok(pendingVaccinationsPage);
    }

    @Operation(summary = "Lấy URL truy cập (đã ký) cho file bằng chứng của một bản ghi tiêm chủng",
            description = "Người dùng đã xác thực và có quyền sẽ nhận được một URL tạm thời để truy cập file.")
    @ApiResponse(responseCode = "200", description = "Thành công, trả về URL đã ký trong một đối tượng JSON {'url': 'SIGNED_URL'}",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "object", example = "{\"url\": \"https://res.cloudinary.com/...SIGNED_URL...\"}")))
    @ApiResponse(responseCode = "403", description = "Không có quyền truy cập file này",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Bản ghi tiêm chủng hoặc file không tìm thấy",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Lỗi khi tạo URL truy cập file",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponseDto.class)))
    @PreAuthorize("isAuthenticated()") // Service sẽ kiểm tra quyền chi tiết hơn
    @GetMapping("/vaccinations/{vaccinationId}/file-access-url")
    public ResponseEntity<Map<String, String>> getProofFileAccessUrl(
            @Parameter(description = "ID của bản ghi tiêm chủng") @PathVariable Long vaccinationId) {
        log.info("API GET /api/vaccinations/{}/file-access-url được gọi", vaccinationId);
        String signedUrl = vaccinationService.getSignedUrlForProofFile(vaccinationId);

        if (signedUrl == null) {
            log.warn("Không thể tạo signed URL hoặc không có file cho vaccinationId: {}", vaccinationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Không tìm thấy file bằng chứng hoặc lỗi tạo URL."));
        }

        return ResponseEntity.ok(Map.of("url", signedUrl));
    }
}