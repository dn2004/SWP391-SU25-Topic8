package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.AdministerMedicationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.ScheduledMedicationTaskResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.SkipMedicationTaskRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.ScheduledMedicationTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mapstruct.ObjectFactory;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/scheduled-medication-tasks")
@RequiredArgsConstructor
@Tag(name = "Quản lý Thực Hiện Lịch Uống Thuốc", description = "API cho NVYT ghi nhận việc cho học sinh uống thuốc")
public class ScheduledMedicationTaskController {
    private final ScheduledMedicationTaskService taskService;

    @Operation(summary = "NVYT xác nhận đã cho học sinh uống thuốc theo lịch",
            description = "Cập nhật trạng thái của một ScheduledMedicationTask thành ADMINISTERED, ghi nhận thông tin, và upload file bằng chứng (nếu có). Yêu cầu vai trò MedicalStaff hoặc StaffManager.")
    @ApiResponse(responseCode = "200", description = "Xác nhận cho uống thuốc thành công",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledMedicationTaskResponseDto.class)))
    @PostMapping(value = "/{taskId}/administer", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<ScheduledMedicationTaskResponseDto> administerMedicationTask(
            @Parameter(description = "ID của lịch uống thuốc cần xác nhận") @PathVariable Long taskId,
            @ModelAttribute @Valid AdministerMedicationRequestDto requestDto) {
        ScheduledMedicationTaskResponseDto response = taskService.administerMedicationTask(taskId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "NVYT đánh dấu một lịch uống thuốc là đã bỏ qua",
            description = "Cập nhật trạng thái của một ScheduledMedicationTask thành một trong các trạng thái SKIPPED. Nếu lý do bỏ qua là do học sinh, hệ thống có thể cố gắng dời lịch. Yêu cầu vai trò MedicalStaff hoặc StaffManager.")
    @ApiResponse(responseCode = "200", description = "Đánh dấu bỏ qua thành công",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ScheduledMedicationTaskResponseDto.class)))
    // Thêm các ApiResponses cho 400, 401, 403, 404
    @PostMapping("/{taskId}/skip")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<ScheduledMedicationTaskResponseDto> skipMedicationTask(
            @Parameter(description = "ID của lịch uống thuốc cần bỏ qua") @PathVariable Long taskId,
            @Valid @RequestBody SkipMedicationTaskRequestDto requestDto) {
        ScheduledMedicationTaskResponseDto response = taskService.skipMedicationTask(taskId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "NVYT lấy danh sách các lịch uống thuốc cần thực hiện trong ngày",
            description = "Lấy danh sách (phân trang) các nhiệm vụ uống thuốc có trạng thái SCHEDULED cho một ngày cụ thể (mặc định là ngày hiện tại nếu không cung cấp). Yêu cầu vai trò MedicalStaff, StaffManager, hoặc SchoolAdmin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách nhiệm vụ thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping("/today")
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

    @Operation(summary = "Lấy lịch sử các task đã xử lý bởi một nhân viên y tế",
            description = "StaffManager/SchoolAdmin có thể xem của bất kỳ nhân viên nào. MedicalStaff chỉ có thể tự xem của mình. Lọc theo khoảng thời gian xử lý (administeredAt) và trạng thái.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên y tế")
    })
    @GetMapping("/handled-by-staff/{staffId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getHandledTasksByStaff(
            @Parameter(description = "ID của nhân viên y tế") @PathVariable Long staffId,
            @Parameter(description = "Trạng thái của task cần lọc (ADMINISTERED, SKIPPED_STUDENT_ABSENT, v.v...)")
            @RequestParam(required = false) Optional<ScheduledMedicationTaskStatus> status,
            @Parameter(description = "Ngày bắt đầu khoảng lọc (YYYY-MM-DDTHH:mm:ss), ví dụ: 2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,
            @Parameter(description = "Ngày kết thúc khoảng lọc (YYYY-MM-DDTHH:mm:ss), ví dụ: 2023-01-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate,
            @PageableDefault(size = 20, sort = "administeredAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<ScheduledMedicationTaskResponseDto> tasksPage = taskService.getHandledTasksByStaff(
                staffId, startDate, endDate, status, pageable);
        return ResponseEntity.ok(tasksPage);
    }

    @Operation(summary = "Lấy lịch sử uống thuốc của một học sinh cụ thể",
            description = "Lấy danh sách (phân trang) các nhiệm vụ uống thuốc đã được xử lý (hoàn thành, bỏ qua, v.v.) của một học sinh. " +
                    "Phụ huynh chỉ có thể xem lịch sử của con mình. " +
                    "NVYT, Quản lý NVYT, và Admin trường có thể xem của bất kỳ học sinh nào.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (ví dụ: phụ huynh xem lịch sử của học sinh khác)"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh")
    })
    @GetMapping("/student/{studentId}/history")
    @PreAuthorize("isAuthenticated()") // Bất kỳ ai đã đăng nhập đều có thể gọi, nhưng logic bên trong sẽ kiểm tra quyền chi tiết
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getStudentMedicationHistory(
            @Parameter(description = "ID của học sinh cần xem lịch sử")
            @PathVariable Long studentId,

            @Parameter(description = "Lọc theo một trạng thái cụ thể của nhiệm vụ (ví dụ: ADMINISTERED, SKIPPED_STUDENT_ABSENT)")
            @RequestParam(required = false) Optional<ScheduledMedicationTaskStatus> status,

            @Parameter(description = "Lọc lịch sử từ ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,

            @Parameter(description = "Lọc lịch sử đến ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate,

            @ParameterObject
            @PageableDefault(size = 15, sort = "administeredAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // Service sẽ chứa logic phức tạp, bao gồm cả việc kiểm tra quyền hạn
        Page<ScheduledMedicationTaskResponseDto> historyPage = taskService.getStudentTaskHistory(
                studentId, status, startDate, endDate, pageable);

        return ResponseEntity.ok(historyPage);
    }

    @Operation(summary = "Lấy lịch sử tất cả các task đã xử lý trong toàn hệ thống",
            description = "API dành cho Quản lý NVYT và Admin trường để xem, lọc và phân trang qua tất cả các nhiệm vụ đã được xử lý. " +
                    "Đây là một API mạnh mẽ cho việc báo cáo và giám sát toàn cục.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách lịch sử thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (vai trò không phù hợp)")
    })
    @GetMapping("/all-history")
    @PreAuthorize("hasAnyRole('StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<ScheduledMedicationTaskResponseDto>> getAllHandledTasksHistory(
            @Parameter(description = "Lọc theo ID của học sinh")
            @RequestParam(required = false) Optional<Long> studentId,

            @Parameter(description = "Lọc theo ID của nhân viên đã xử lý")
            @RequestParam(required = false) Optional<Long> staffId,

            @Parameter(description = "Lọc theo một trạng thái cụ thể của nhiệm vụ (ví dụ: ADMINISTERED)")
            @RequestParam(required = false) Optional<ScheduledMedicationTaskStatus> status,

            @Parameter(description = "Lọc lịch sử từ ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> startDate,

            @Parameter(description = "Lọc lịch sử đến ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> endDate,

            @ParameterObject
            @PageableDefault(size = 20, sort = "administeredAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // Lời gọi service cũng được cập nhật
        Page<ScheduledMedicationTaskResponseDto> historyPage = taskService.getAllHandledTasksHistory(
                studentId, staffId, status, startDate, endDate, pageable);

        return ResponseEntity.ok(historyPage);
    }
}
