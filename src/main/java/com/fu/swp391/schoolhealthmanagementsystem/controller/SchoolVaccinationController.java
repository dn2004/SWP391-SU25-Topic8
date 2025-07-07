package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.SchoolVaccinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccination/records")
@RequiredArgsConstructor
@Tag(name = "Quản lý Tiêm chủng tại Trường", description = "API cho việc ghi nhận tiêm chủng và theo dõi sau tiêm")
@SecurityRequirement(name = "bearerAuth")
public class SchoolVaccinationController {

    private final SchoolVaccinationService schoolVaccinationService;

    @Operation(summary = "Ghi nhận kết quả tiêm chủng",
            description = "Ghi nhận việc học sinh đã tiêm, vắng mặt hoặc từ chối tiêm. Yêu cầu quyền MedicalStaff.")
    @ApiResponse(responseCode = "200", description = "Ghi nhận thành công",
            content = @Content(schema = @Schema(implementation = SchoolVaccinationResponseDto.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<SchoolVaccinationResponseDto> recordVaccination(
            @Valid @RequestBody RecordVaccinationRequestDto requestDto) {
        return ResponseEntity.ok(schoolVaccinationService.recordVaccination(requestDto));
    }

    @Operation(summary = "Cập nhật trạng thái tiêm chủng",
            description = "Cập nhật trạng thái của một bản ghi tiêm chủng. Chỉ có thể cập nhật khi campaign đang IN_PROGRESS.")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = SchoolVaccinationResponseDto.class)))
    @PutMapping("/{vaccinationId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<SchoolVaccinationResponseDto> updateVaccinationRecord(
            @PathVariable Long vaccinationId,
            @Valid @RequestBody UpdateVaccinationRecordRequestDto requestDto) {
        return ResponseEntity.ok(schoolVaccinationService.updateVaccinationRecord(vaccinationId, requestDto));
    }

    @Operation(summary = "Ghi nhận kết quả theo dõi sau tiêm",
            description = "Ghi nhận dữ liệu theo dõi sau tiêm như nhiệt độ, phản ứng phụ. Yêu cầu quyền MedicalStaff.")
    @ApiResponse(responseCode = "200", description = "Ghi nhận thành công",
            content = @Content(schema = @Schema(implementation = PostVaccinationMonitoringResponseDto.class)))
    @PostMapping("/monitoring")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<PostVaccinationMonitoringResponseDto> recordMonitoring(
            @Valid @RequestBody CreatePostVaccinationMonitoringRequestDto requestDto) {
        return ResponseEntity.ok(schoolVaccinationService.recordPostVaccinationMonitoring(requestDto));
    }

    @Operation(summary = "Cập nhật bản ghi theo dõi sau tiêm",
            description = "Cập nhật thông tin theo dõi sau tiêm chủng. Chỉ có thể cập nhật khi vaccination đang POST_MONITORING.")
    @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
            content = @Content(schema = @Schema(implementation = PostVaccinationMonitoringResponseDto.class)))
    @PutMapping("/monitoring/{monitoringId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager')")
    public ResponseEntity<PostVaccinationMonitoringResponseDto> updateMonitoring(
            @PathVariable Long monitoringId,
            @Valid @RequestBody UpdatePostVaccinationMonitoringRequestDto requestDto) {
        return ResponseEntity.ok(schoolVaccinationService.updatePostVaccinationMonitoring(monitoringId, requestDto));
    }

    @Operation(summary = "Lấy danh sách tiêm chủng của một chiến dịch",
            description = "Lấy danh sách các bản ghi tiêm chủng trong một chiến dịch với phân trang.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/campaign/{campaignId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    public ResponseEntity<Page<SchoolVaccinationResponseDto>> getVaccinationsForCampaign(
            @PathVariable Long campaignId,
            @Parameter(description = "Lọc theo tên học sinh") @RequestParam(required = false) String studentName,
            @Parameter(description = "Lọc theo tên lớp (A, B, C...)") @RequestParam(required = false) String className,
            @Parameter(description = "Lọc theo trạng thái tiêm chủng") @RequestParam(required = false) SchoolVaccinationStatus status,
            @ParameterObject
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(schoolVaccinationService.getVaccinationsForCampaign(campaignId, studentName, className, status, pageable));
    }

    @Operation(summary = "Lấy chi tiết bản ghi tiêm chủng",
            description = "Lấy thông tin chi tiết của một bản ghi tiêm chủng theo ID.")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(schema = @Schema(implementation = SchoolVaccinationResponseDto.class)))
    @GetMapping("/{vaccinationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SchoolVaccinationResponseDto> getVaccinationById(
            @PathVariable Long vaccinationId) {
        return ResponseEntity.ok(schoolVaccinationService.getVaccinationById(vaccinationId));
    }

    @Operation(summary = "Lấy danh sách theo dõi sau tiêm",
            description = "Lấy bản ghi theo dõi sau tiêm cho một bản ghi tiêm chủng cụ thể.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/{vaccinationId}/monitoring")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostVaccinationMonitoringResponseDto> getMonitoringForVaccination(
            @PathVariable Long vaccinationId) {
        return ResponseEntity.ok(schoolVaccinationService.getMonitoringForVaccination(vaccinationId));
    }
}
