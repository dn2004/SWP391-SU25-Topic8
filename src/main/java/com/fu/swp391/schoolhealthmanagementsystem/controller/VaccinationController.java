package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.VaccinationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/vaccination")
@RequiredArgsConstructor
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @PostMapping("/campaigns")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Tạo chiến dịch tiêm chủng mới",
               description = "Tạo một chiến dịch tiêm chủng mới với thông tin về vaccine, thời gian, đối tượng và người tổ chức. Chỉ admin, quản lý nhân viên và nhân viên y tế mới có quyền thực hiện.")
    public ResponseEntity<VaccinationCampaignResponseDto> createVaccinationCampaign(
            @Valid @RequestBody CreateVaccinationCampaignRequestDto requestDto) {
        VaccinationCampaignResponseDto campaign = vaccinationService.createVaccinationCampaign(requestDto);
        return new ResponseEntity<>(campaign, HttpStatus.CREATED);
    }

    @PutMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Cập nhật chiến dịch tiêm chủng (chỉ khi ở trạng thái DRAFT)",
               description = "Cập nhật thông tin chiến dịch tiêm chủng. Chỉ có thể cập nhật khi chiến dịch đang ở trạng thái DRAFT (bản nháp). Sau khi lên lịch sẽ không thể chỉnh sửa.")
    public ResponseEntity<VaccinationCampaignResponseDto> updateVaccinationCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody CreateVaccinationCampaignRequestDto requestDto) {
        VaccinationCampaignResponseDto updatedCampaign = vaccinationService.updateVaccinationCampaign(campaignId, requestDto);
        return ResponseEntity.ok(updatedCampaign);
    }

    @GetMapping("/campaigns/{campaignId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy thông tin chiến dịch tiêm chủng theo ID",
               description = "Lấy thông tin chi tiết của một chiến dịch tiêm chủng cụ thể bao gồm tên chiến dịch, vaccine, thời gian, trạng thái và người tổ chức.")
    public ResponseEntity<VaccinationCampaignResponseDto> getVaccinationCampaignById(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.getVaccinationCampaignById(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @GetMapping("/campaigns")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy danh sách tất cả chiến dịch tiêm chủng có lọc và phân trang",
               description = "Lấy danh sách chiến dịch tiêm chủng với khả năng lọc theo tên chiến dịch, tên vaccine, trạng thái, thời gian, lớp học và người tổ chức. Hỗ trợ phân trang để hiển thị kết quả.")
    public ResponseEntity<Page<VaccinationCampaignResponseDto>> getAllVaccinationCampaigns(
            @PageableDefault(size = 10, page = 0, sort = "createdAt")
            @ParameterObject
            Pageable pageable,
            @RequestParam(required = false) String campaignName,
            @RequestParam(required = false) String vaccineName,
            @RequestParam(required = false) VaccinationCampaignStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ClassGroup classGroup,
            @RequestParam(required = false) Long organizedByUserId) {
        Page<VaccinationCampaignResponseDto> campaigns = vaccinationService.getAllVaccinationCampaigns(
                pageable, campaignName, vaccineName, status, startDate, endDate, classGroup, organizedByUserId);
        return ResponseEntity.ok(campaigns);
    }

    // --- Refactored Status Change Endpoints ---

    @PostMapping("/campaigns/{campaignId}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Lên lịch chiến dịch (DRAFT -> SCHEDULED)",
               description = "Chuyển trạng thái chiến dịch từ DRAFT (bản nháp) sang SCHEDULED (đã lên lịch). Sau khi lên lịch, chiến dịch sẽ không thể chỉnh sửa thông tin cơ bản nữa.")
    public ResponseEntity<VaccinationCampaignResponseDto> scheduleCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.scheduleCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Bắt đầu chiến dịch (PREPARING -> IN_PROGRESS)",
               description = "Bắt đầu thực hiện chiến dịch tiêm chủng, chuyển trạng thái từ PREPARING (chuẩn bị) sang IN_PROGRESS (đang thực hiện). Sau khi bắt đầu có thể tiến hành tiêm chủng cho học sinh.")
    public ResponseEntity<VaccinationCampaignResponseDto> startCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.startCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Hoàn thành chiến dịch (IN_PROGRESS -> COMPLETED)",
               description = "Đánh dấu chiến dịch tiêm chủng đã hoàn thành, chuyển trạng thái từ IN_PROGRESS (đang thực hiện) sang COMPLETED (đã hoàn thành). Chiến dịch đã hoàn thành sẽ không thể thay đổi.")
    public ResponseEntity<VaccinationCampaignResponseDto> completeCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.completeCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Hủy chiến dịch tiêm chủng",
               description = "Hủy bỏ chiến dịch tiêm chủng ở bất kỳ trạng thái nào (trừ COMPLETED). Chiến dịch đã hủy sẽ không thể khôi phục và không thể thực hiện tiêm chủng.")
    public ResponseEntity<VaccinationCampaignResponseDto> cancelCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.cancelCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Thay đổi lịch chiến dịch (chỉ khi ở trạng thái PREPARING)",
               description = "Thay đổi thời gian thực hiện chiến dịch tiêm chủng. Chỉ có thể thực hiện khi chiến dịch đang ở trạng thái PREPARING (chuẩn bị). Cần cung cấp ngày và giờ mới.")
    public ResponseEntity<VaccinationCampaignResponseDto> rescheduleCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody RescheduleCampaignRequestDto requestDto) {
        VaccinationCampaignResponseDto campaign = vaccinationService.rescheduleCampaign(campaignId, requestDto);
        return ResponseEntity.ok(campaign);
    }

    // --- Consent Endpoints ---

    @GetMapping("/campaigns/{campaignId}/consents")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Lấy tất cả phiếu đồng ý của một chiến dịch",
               description = "Lấy danh sách tất cả phiếu đồng ý tiêm chủng của học sinh trong một chiến dịch cụ thể. Bao gồm thông tin học sinh, trạng thái đồng ý và ghi chú từ phụ huynh. Có thể lọc theo tên học sinh và lớp học.")
    public ResponseEntity<Page<VaccinationConsentResponseDto>> getConsentsForCampaign(
            @PathVariable Long campaignId,
            @PageableDefault(size = 10, page = 0, sort = "createdAt")
            @ParameterObject
            Pageable pageable,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String className) {
        Page<VaccinationConsentResponseDto> consents = vaccinationService.getConsentsForCampaign(campaignId, pageable, studentName, className);
        return ResponseEntity.ok(consents);
    }

    @GetMapping("/consents/{consentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy thông tin phiếu đồng ý theo ID",
               description = "Lấy thông tin chi tiết của một phiếu đồng ý tiêm chủng cụ thể bao gồm thông tin học sinh, phụ huynh, trạng thái đồng ý và lý do (nếu có).")
    public ResponseEntity<VaccinationConsentResponseDto> getConsentById(@PathVariable Long consentId) {
        VaccinationConsentResponseDto consent = vaccinationService.getConsentById(consentId);
        return ResponseEntity.ok(consent);
    }

    @PutMapping("/consents/{consentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF_MANAGER', 'MEDICAL_STAFF')")
    @Operation(summary = "Cập nhật phản hồi phiếu đồng ý (bởi nhân viên)",
               description = "Cập nhật trạng thái và thông tin phản hồi của phiếu đồng ý tiêm chủng. Chỉ nhân viên y tế có thể cập nhật để ghi nhận kết quả tiêm chủng hoặc lý do không tiêm.")
    public ResponseEntity<VaccinationConsentResponseDto> updateConsentResponse(
            @PathVariable Long consentId,
            @Valid @RequestBody UpdateVaccinationConsentRequestDto requestDto) {
        VaccinationConsentResponseDto updatedConsent = vaccinationService.updateConsentResponse(consentId, requestDto);
        return ResponseEntity.ok(updatedConsent);
    }
}
