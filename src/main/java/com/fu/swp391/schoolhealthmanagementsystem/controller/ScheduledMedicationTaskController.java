package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.AdministerMedicationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.ScheduledMedicationTaskResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.SkipMedicationTaskRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.ScheduledMedicationTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/scheduled-medication-tasks")
@RequiredArgsConstructor
@Tag(name = "Scheduled Medication Task Management", description = "API cho NVYT ghi nhận việc cho học sinh uống thuốc")
@SecurityRequirement(name = "bearerAuth")
public class ScheduledMedicationTaskController {
    private final ScheduledMedicationTaskService taskService;

    @Operation(
        summary = "Lấy URL truy cập tạm thời cho file bằng chứng của một task",
        description = """
### Mô tả
Trả về một URL có thời hạn để truy cập file bằng chứng (ảnh/video) đã được upload cho một nhiệm vụ uống thuốc.
- **Phân quyền:** 
    - `Parent`: Chỉ xem được của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ ai.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy URL thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object", example = "{\"url\": \"https://...\"}"))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy task hoặc không có file bằng chứng", content = @Content)
    })
    @GetMapping("/{taskId}/proof-url")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> getTaskProofAccessUrl(
            @Parameter(description = "ID của task cần lấy URL bằng chứng") @PathVariable Long taskId) {
        String url = taskService.getTaskProofAccessUrl(taskId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @Operation(
        summary = "NVYT xác nhận đã cho học sinh uống thuốc theo lịch",
        description = """
### Mô tả
Cập nhật trạng thái của một nhiệm vụ uống thuốc thành `ADMINISTERED`, ghi nhận thông tin, và upload file bằng chứng (nếu có).
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh của học sinh khi xác nhận thành công.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xác nhận cho uống thuốc thành công",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledMedicationTaskResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy task", content = @Content)
    })
    @PostMapping(value = "/{taskId}/administer", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<ScheduledMedicationTaskResponseDto> administerMedicationTask(
            @Parameter(description = "ID của lịch uống thuốc cần xác nhận") @PathVariable Long taskId,
            @ModelAttribute @Valid AdministerMedicationRequestDto requestDto) {
        ScheduledMedicationTaskResponseDto response = taskService.administerMedicationTask(taskId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "NVYT đánh dấu một lịch uống thuốc là đã bỏ qua",
        description = """
### Mô tả
Cập nhật trạng thái của một nhiệm vụ uống thuốc thành một trong các trạng thái `SKIPPED`.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh của học sinh khi đánh dấu bỏ qua.
"""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đánh dấu bỏ qua thành công",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledMedicationTaskResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy task", content = @Content)
    })
    @PostMapping("/{taskId}/skip")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<ScheduledMedicationTaskResponseDto> skipMedicationTask(
            @Parameter(description = "ID của lịch uống thuốc cần bỏ qua") @PathVariable Long taskId,
            @Valid @RequestBody SkipMedicationTaskRequestDto requestDto) {
        ScheduledMedicationTaskResponseDto response = taskService.skipMedicationTask(taskId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "NVYT lấy danh sách các lịch uống thuốc cần thực hiện trong ngày",
        description = """
### Mô tả
Lấy danh sách các nhiệm vụ uống thuốc có trạng thái `SCHEDULED` cho một ngày cụ thể (mặc định là ngày hiện tại).
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách nhiệm vụ thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping("")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getDailyScheduledTasks(
            @Parameter(description = "Ngày cần lấy danh sách nhiệm vụ (YYYY-MM-DD). Nếu bỏ trống, mặc định là ngày hiện tại.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(size = 20, sort = "scheduledTimeText", direction = Sort.Direction.ASC)
            @ParameterObject
            Pageable pageable) {
        Page<ScheduledMedicationTaskResponseDto> tasksPage = taskService.getScheduledTasksForDate(date, pageable);
        return ResponseEntity.ok(tasksPage);
    }

    @Operation(
        summary = "Lấy lịch sử các task đã xử lý bởi một nhân viên y tế",
        description = """
### Mô tả
Lấy lịch sử các nhiệm vụ uống thuốc đã được xử lý bởi một nhân viên y tế cụ thể.
- **Phân quyền:** 
    - `MedicalStaff`: Chỉ có thể tự xem của mình.
    - `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ nhân viên nào.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên y tế", content = @Content)
    })
    @GetMapping("/handled-by-staff/{staffId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getHandledTasksByStaff(
            @Parameter(description = "ID của nhân viên y tế") @PathVariable Long staffId,
            @Parameter(description = "Trạng thái của task cần lọc (ADMINISTERED, SKIPPED_STUDENT_ABSENT, v.v...)")
            @RequestParam(required = false) ScheduledMedicationTaskStatus status,
            @Parameter(description = "Ngày bắt đầu khoảng lọc (YYYY-MM-DDTHH:mm:ss), ví dụ: 2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Ngày kết thúc khoảng lọc (YYYY-MM-DDTHH:mm:ss), ví dụ: 2023-01-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "administeredAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ScheduledMedicationTaskResponseDto> tasksPage = taskService.getHandledTasksByStaff(
                staffId, startDate, endDate, status, pageable);
        return ResponseEntity.ok(tasksPage);
    }

    @Operation(
        summary = "Lấy lịch sử uống thuốc của một học sinh cụ thể",
        description = """
### Mô tả
Lấy danh sách các nhiệm vụ uống thuốc đã được xử lý (hoàn thành, bỏ qua,...) của một học sinh.
- **Phân quyền:**
    - `Parent`: Chỉ có thể xem lịch sử của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ học sinh nào.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (ví dụ: phụ huynh xem lịch sử của học sinh khác)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @GetMapping("/student/{studentId}/history")
    @PreAuthorize("isAuthenticated()") // Bất kỳ ai đã đăng nhập đều có thể gọi, nhưng logic bên trong sẽ kiểm tra quyền chi tiết
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getStudentMedicationHistory(
            @Parameter(description = "ID của học sinh cần xem lịch sử")
            @PathVariable Long studentId,

            @Parameter(description = "Lọc theo một trạng thái cụ thể của nhiệm vụ (ví dụ: ADMINISTERED, SKIPPED_STUDENT_ABSENT)")
            @RequestParam(required = false) ScheduledMedicationTaskStatus status,

            @Parameter(description = "Lọc lịch sử từ ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Lọc lịch sử đến ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @ParameterObject
            @PageableDefault(size = 15, sort = "administeredAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // Service sẽ chứa logic phức tạp, bao gồm cả việc kiểm tra quyền hạn
        Page<ScheduledMedicationTaskResponseDto> historyPage = taskService.getStudentTaskHistory(
                studentId, status, startDate, endDate, pageable);

        return ResponseEntity.ok(historyPage);
    }

    @Operation(
        summary = "Lấy lịch sử tất cả các task đã xử lý trong toàn hệ thống",
        description = """
### Mô tả
API dành cho quản lý và admin để xem, lọc và phân trang qua tất cả các nhiệm vụ đã được xử lý trong hệ thống.
- **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách lịch sử thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (vai trò không phù hợp)", content = @Content)
    })
    @GetMapping("/all-history")
    @PreAuthorize("hasAnyRole('StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getAllHandledTasksHistory(
            @Parameter(description = "Lọc theo ID của học sinh")
            @RequestParam(required = false) Long studentId,

            @Parameter(description = "Lọc theo ID của nhân viên đã xử l��")
            @RequestParam(required = false) Long staffId,

            @Parameter(description = "Lọc theo một trạng thái cụ thể của nhiệm vụ (ví dụ: ADMINISTERED)")
            @RequestParam(required = false) ScheduledMedicationTaskStatus status,

            @Parameter(description = "Lọc lịch sử từ ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "Lọc lịch sử đến ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @ParameterObject
            @PageableDefault(size = 20, sort = "administeredAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // Lời gọi service cũng được cập nhật
        Page<ScheduledMedicationTaskResponseDto> historyPage = taskService.getAllHandledTasksHistory(
                studentId, staffId, status, startDate, endDate, pageable);

        return ResponseEntity.ok(historyPage);
    }
}
