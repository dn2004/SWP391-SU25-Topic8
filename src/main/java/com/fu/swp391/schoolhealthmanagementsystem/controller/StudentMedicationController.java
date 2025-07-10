package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentMedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-medications")
@RequiredArgsConstructor
@Tag(name = "Quản lý Thuốc của Học Sinh", description = "Các API liên quan đến quản lý thuốc của học sinh")
@Slf4j
public class StudentMedicationController {

    private final StudentMedicationService studentMedicationService;

    @Operation(summary = "NVYT tạo bản ghi thuốc và lịch trình ban đầu cho học sinh",
            description = """
### Mô tả
Nhân viên y tế nhập thông tin thuốc do phụ huynh gửi, bao gồm tên thuốc, số liều, hướng dẫn và lịch trình uống thuốc. Trạng thái thuốc mặc định là `AVAILABLE`.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh khi tạo thành công.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo bản ghi thuốc thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (vai trò không phù hợp hoặc phụ huynh không liên kết với học sinh)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh hoặc phụ huynh", content = @Content)
    })
    @PostMapping("/staff-create")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<StudentMedicationResponseDto> createStudentMedicationByStaff(
            @Valid @RequestBody CreateStudentMedicationByStaffRequestDto requestDto) {
        StudentMedicationResponseDto response = studentMedicationService.createStudentMedicationByStaff(requestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin chi tiết một bản ghi thuốc của học sinh",
            description = """
### Mô tả
Lấy thông tin chi tiết của một bản ghi thuốc dựa trên ID.
- **Phân quyền:** 
    - `Parent`: Chỉ xem được của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ ai.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm thấy thông tin thuốc",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông tin thuốc", content = @Content)
    })
    @GetMapping("/{studentMedicationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StudentMedicationResponseDto> getStudentMedicationById(
            @Parameter(description = "ID của bản ghi thuốc học sinh cần lấy") @PathVariable Long studentMedicationId) {
        StudentMedicationResponseDto response = studentMedicationService.getStudentMedicationById(studentMedicationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy lịch sử giao dịch của một đơn thuốc",
            description = """
### Mô tả
Lấy danh sách các giao dịch liên quan đến một đơn thuốc cụ thể (nhập, dùng, hủy,...).
- **Phân quyền:** 
    - `Parent`: Chỉ xem được của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ ai.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy lịch sử giao dịch thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy đơn thuốc", content = @Content)
    })
    @GetMapping("/{studentMedicationId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<StudentMedicationTransactionResponseDto>> getTransactionsForStudentMedication(
            @Parameter(description = "ID của đơn thuốc cần xem lịch sử") @PathVariable Long studentMedicationId,
            @Parameter(description = "Lọc theo loại giao dịch")
            @RequestParam(required = false) StudentMedicationTransactionType transactionType,
            @Parameter(description = "Lọc từ thời điểm (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @Parameter(description = "Lọc đến thời điểm (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @ParameterObject @PageableDefault(size = 10, sort = "transactionDateTime", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<StudentMedicationTransactionResponseDto> response = studentMedicationService.getTransactionsForStudentMedication(
            studentMedicationId,
            startDateTime,
            endDateTime,
            transactionType,
            pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "NVYT cập nhật thông tin cơ bản của thuốc học sinh",
            description = """
### Mô tả
Cập nhật các thông tin mô tả như tên thuốc, liều dùng (text), ngày hết hạn, ghi chú. Không dùng để cập nhật số liều hoặc lịch trình.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi thuốc", content = @Content)
    })
    @PutMapping("/{studentMedicationId}/info")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<StudentMedicationResponseDto> updateStudentMedicationInfo(
            @Parameter(description = "ID của bản ghi thuốc cần cập nhật thông tin") @PathVariable Long studentMedicationId,
            @Valid @RequestBody UpdateStudentMedicationInfoRequestDto requestDto) {
        StudentMedicationResponseDto response = studentMedicationService.updateStudentMedicationInfo(studentMedicationId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cập nhật thông tin lịch trình cho thuốc của học sinh",
            description = """
### Mô tả
Cập nhật ngày bắt đầu, các cữ uống trong ngày. Các task cũ trong tương lai sẽ bị hủy và task mới sẽ được tạo.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh khi cập nhật lịch trình.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi thuốc", content = @Content)
    })
    @PutMapping("/{studentMedicationId}/schedule")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<StudentMedicationResponseDto> updateMedicationSchedule(
            @Parameter(description = "ID của StudentMedication cần cập nhật lịch") @PathVariable Long studentMedicationId,
            @Valid @RequestBody UpdateMedicationScheduleRequestDto scheduleDto) {
        StudentMedicationResponseDto updatedMedication = studentMedicationService.updateMedicationSchedule(studentMedicationId, scheduleDto);
        return ResponseEntity.ok(updatedMedication);
    }

    @Operation(summary = "Báo cáo thuốc của học sinh bị thất lạc",
            description = """
### Mô tả
Báo cáo thuốc bị thất lạc. Trạng thái thuốc sẽ thành `LOST`, số liều về 0, các lịch trình tương lai bị hủy.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh về việc thuốc bị thất lạc.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Báo cáo thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi thuốc", content = @Content)
    })
    @PostMapping("/{studentMedicationId}/report-lost")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<StudentMedicationResponseDto> reportLostMedication(
            @Parameter(description = "ID của StudentMedication bị thất lạc") @PathVariable Long studentMedicationId,
            @Valid @RequestBody ReportLostMedicationRequestDto requestDto) {
        StudentMedicationResponseDto response = studentMedicationService.reportLostMedication(studentMedicationId, requestDto);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xác nhận trả thuốc lại cho phụ huynh",
            description = """
### Mô tả
Xác nhận đã trả lại thuốc cho phụ huynh. Trạng thái thuốc sẽ thành `RETURNED_TO_PARENT`, số liều về 0, các lịch trình tương lai bị hủy.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh về việc đã trả thuốc.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác nhận thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bản ghi thuốc", content = @Content)
    })
    @PostMapping("/{studentMedicationId}/return-to-parent")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<StudentMedicationResponseDto> returnMedicationToParent(
            @Parameter(description = "ID của StudentMedication được trả lại") @PathVariable Long studentMedicationId,
            @Valid @RequestBody ReturnMedicationToParentRequestDto requestDto) {
        StudentMedicationResponseDto response = studentMedicationService.returnMedicationToParent(studentMedicationId, requestDto);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Hủy bỏ thuốc đã nhập",
            description = """
### Mô tả
Hủy bỏ một đơn thuốc đã nhập.
- **Điều kiện:** Chỉ người tạo mới được hủy và thuốc chưa được lên lịch.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff` hoặc `StaffManager`.
- **Thông báo:** Gửi thông báo đến phụ huynh về việc hủy thuốc.
"""
    )
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Hủy thuốc thành công",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentMedicationResponseDto.class))),
    @ApiResponse(responseCode = "400", description = "Không thể hủy (thuốc đã có lịch hoặc không ở trạng thái cho phép)", content = @Content),
    @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
    @ApiResponse(responseCode = "403", description = "Không có quyền hủy (không phải người tạo thuốc)", content = @Content),
    @ApiResponse(responseCode = "404", description = "Không tìm thấy thuốc với ID cung cấp", content = @Content)
    })
    @PostMapping("/{studentMedicationId}/cancel")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<StudentMedicationResponseDto> cancelStudentMedication(
            @Parameter(description = "ID của thuốc cần hủy") @PathVariable Long studentMedicationId,
            @Valid @RequestBody CancelStudentMedicationRequestDto requestDto) {

        StudentMedicationResponseDto result = studentMedicationService.cancelStudentMedication(studentMedicationId, requestDto);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy danh sách thuốc của một học sinh cụ thể",
            description = """
### Mô tả
Lấy danh sách tất cả các loại thuốc đã được gửi cho một học sinh.
- **Phân quyền:** 
    - `Parent`: Chỉ xem được của con mình.
    - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ ai.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @GetMapping("/student/{studentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<StudentMedicationResponseDto>> getMedicationsByStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @Parameter(description = "Lọc từ ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,
            @Parameter(description = "Lọc đến ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,
            @ParameterObject
            @PageableDefault(size = 10, sort = "dateReceived", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<StudentMedicationResponseDto> response = studentMedicationService.getMedicationsByStudentId(studentId, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Lấy tất cả bản ghi thuốc của học sinh trong hệ thống (cho nhân viên)",
            description = """
### Mô tả
Lấy danh sách tất cả các đơn thuốc của học sinh trong hệ thống.
- **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<StudentMedicationResponseDto>> getAllStudentMedications(
            @Parameter(description = "Lọc theo trạng thái thuốc")
            @RequestParam(required = false) MedicationStatus status,

            @Parameter(description = "Lọc từ ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate,

            @Parameter(description = "Lọc đến ngày (YYYY-MM-DDTHH:mm:ss)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate endDate,

            @ParameterObject
            @PageableDefault(size = 10, sort = "dateReceived", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("status: {}, startDate: {}, endDate: {}", status, startDate, endDate);

        Page<StudentMedicationResponseDto> response = studentMedicationService.getAllStudentMedications(status, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }
}

