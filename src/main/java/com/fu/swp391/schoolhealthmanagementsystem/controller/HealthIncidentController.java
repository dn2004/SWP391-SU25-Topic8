package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.CreateHealthIncidentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.HealthIncidentResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.UpdateHealthIncidentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import com.fu.swp391.schoolhealthmanagementsystem.service.HealthIncidentService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/health-incidents")
@RequiredArgsConstructor
@Tag(name = "Quản lý Sự cố Sức khỏe", description = "API cho việc quản lý các sự cố sức khỏe của học sinh")
@SecurityRequirement(name = "bearerAuth")
public class HealthIncidentController {

    private final HealthIncidentService healthIncidentService;

    @Operation(summary = "Tạo mới một sự cố sức khỏe",
            description = """
Ghi nhận một sự cố sức khỏe mới cho học sinh.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh của học sinh khi tạo thành công.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sự cố sức khỏe được tạo thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HealthIncidentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<HealthIncidentResponseDto> createHealthIncident(
            @Valid @RequestBody CreateHealthIncidentRequestDto requestDto) {
        HealthIncidentResponseDto createdIncident = healthIncidentService.createHealthIncident(requestDto);
        return new ResponseEntity<>(createdIncident, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin sự cố sức khỏe theo ID",
            description = """
Lấy thông tin chi tiết của một sự cố sức khỏe (chưa bị xóa mềm).
- **Phân quyền:**
    - `Parent`: Chỉ có thể xem sự cố của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem bất kỳ sự cố nào.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy sự cố sức khỏe",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthIncidentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sự cố sức khỏe (hoặc đã bị xóa mềm)", content = @Content)
    })
    @GetMapping("/{incidentId}")
    @PreAuthorize("isAuthenticated()") // Logic phân quyền chi tiết hơn (Parent vs Staff) nằm trong service
    public ResponseEntity<HealthIncidentResponseDto> getHealthIncidentById(
            @Parameter(description = "ID của sự cố sức khỏe") @PathVariable Long incidentId) {
        return ResponseEntity.ok(healthIncidentService.getHealthIncidentById(incidentId));
    }

    @Operation(summary = "Lấy danh sách sự cố sức khỏe của một học sinh",
            description = """
Lấy danh sách (phân trang) các sự cố sức khỏe (chưa bị xóa mềm) của một học sinh cụ thể. Hỗ trợ lọc theo loại, địa điểm và khoảng thời gian.
- **Phân quyền:**
    - `Parent`: Chỉ có thể xem danh sách sự cố của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ học sinh nào.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<HealthIncidentResponseDto>> getAllHealthIncidentsByStudentId(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @PageableDefault(size = 10, sort = "incidentDateTime", direction = Sort.Direction.DESC)
            @ParameterObject
            Pageable pageable,
            @Parameter(description = "Lọc theo loại sự cố")
            @RequestParam(required = false) HealthIncidentType incidentType,
            @Parameter(description = "Lọc theo địa điểm")
            @RequestParam(required = false) String location,
            @Parameter(description = "Lọc từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Lọc đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(healthIncidentService.getAllHealthIncidentsByStudentId(studentId, pageable, incidentType, location, startDate, endDate));
    }

    @Operation(summary = "Lấy tất cả sự cố sức khỏe (cho nhân viên)",
            description = """
Lấy danh sách (phân trang) tất cả các sự cố sức khỏe (chưa bị xóa mềm). Hỗ trợ lọc theo nhiều tiêu chí.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<HealthIncidentResponseDto>> getAllHealthIncidents(
            @PageableDefault(size = 10, sort = "incidentDateTime", direction = Sort.Direction.DESC)
            @ParameterObject
            Pageable pageable,
            @Parameter(description = "Lọc theo loại sự cố")
            @RequestParam(required = false) HealthIncidentType incidentType,
            @Parameter(description = "Lọc từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Lọc đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Lọc theo tên học sinh (một phần hoặc toàn bộ)")
            @RequestParam(required = false) String studentName,
            @Parameter(description = "Lọc theo t��n người ghi nhận (một phần hoặc toàn bộ)")
            @RequestParam(required = false) String recordedByName,
            @Parameter(description = "Lọc theo địa điểm")
            @RequestParam(required = false) String location,
            @Parameter(description = "Lọc theo mô tả")
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(healthIncidentService.getAllHealthIncidents(pageable, incidentType, startDate, endDate, studentName, recordedByName, location, description));
    }

    @Operation(summary = "Lấy danh sách sự cố sức khỏe của tôi (cho nhân viên y tế)",
            description = """
Lấy danh sách (phân trang) các sự cố sức khỏe do chính nhân viên y tế/quản lý đang đăng nhập ghi nhận. Hỗ trợ lọc theo nhiều tiêu chí.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ dành cho MedicalStaff)", content = @Content)
    })
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<Page<HealthIncidentResponseDto>> getMyHealthIncidents(
            @PageableDefault(size = 10, sort = "incidentDateTime", direction = Sort.Direction.DESC)
            @ParameterObject
            Pageable pageable,
            @Parameter(description = "Lọc theo loại sự cố")
            @RequestParam(required = false) HealthIncidentType incidentType,
            @Parameter(description = "Lọc từ ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Lọc đến ngày (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Lọc theo tên học sinh (một phần hoặc toàn bộ)")
            @RequestParam(required = false) String studentName,
            @Parameter(description = "Lọc theo địa điểm")
            @RequestParam(required = false) String location,
            @Parameter(description = "Lọc theo mô tả")
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(healthIncidentService.getMyHealthIncidents(pageable, incidentType, startDate, endDate, studentName, location, description));
    }

    @Operation(summary = "Cập nhật một sự cố sức khỏe",
            description = """
Cập nhật thông tin của một sự cố sức khỏe (chưa bị xóa mềm).
- **Điều kiện:** Chỉ có thể cập nhật trong vòng 1 ngày kể từ khi tạo.
- **Phân quyền:**
    - Người tạo sự cố (`MedicalStaff`).
    - `StaffManager`, `SchoolAdmin`.
- **Thông báo:** Gửi thông báo đến phụ huynh của học sinh khi cập nhật thành công.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthIncidentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc không thể cập nhật (ví dụ: quá hạn)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sự cố sức khỏe (hoặc đã bị xóa mềm)", content = @Content)
    })
    @PutMapping("/{incidentId}")
    // Phân quyền trong PreAuthorize có thể chỉ là bước đầu, service sẽ check kỹ hơn ai là người tạo
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<HealthIncidentResponseDto> updateHealthIncident(
            @Parameter(description = "ID của sự cố cần cập nhật") @PathVariable Long incidentId,
            @Valid @RequestBody UpdateHealthIncidentRequestDto updateDto) {
        return ResponseEntity.ok(healthIncidentService.updateHealthIncident(incidentId, updateDto));
    }

    @Operation(summary = "Xóa mềm một sự cố sức khỏe",
            description = """
Đánh dấu một sự cố sức khỏe là đã xóa và hoàn trả lại số lượng vật tư y tế đã sử dụng.
- **Điều kiện:** Chỉ có thể xóa trong ngày tạo sự cố.
- **Phân quyền:**
    - Người tạo sự cố (`MedicalStaff`).
    - `StaffManager`, `SchoolAdmin`.
""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa mềm thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Sự cố đã bị xóa trước đó", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sự cố sức khỏe", content = @Content)
    })
    @DeleteMapping("/{incidentId}")
    @PreAuthorize("hasAnyRole('StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Void> deleteHealthIncident(
            @Parameter(description = "ID của sự cố cần xóa mềm") @PathVariable Long incidentId) {
        healthIncidentService.deleteHealthIncident(incidentId);
        return ResponseEntity.noContent().build();
    }
}