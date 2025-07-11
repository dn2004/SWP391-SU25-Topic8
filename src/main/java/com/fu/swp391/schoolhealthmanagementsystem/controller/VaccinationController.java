package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.VaccinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    @PostMapping("/campaigns")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @Operation(
            summary = "Tạo chiến dịch tiêm chủng mới",
            description = """
                    ### Mô tả
                    Tạo một chiến dịch tiêm chủng mới với thông tin về vaccine, thời gian, đối tượng.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo chiến dịch thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> createVaccinationCampaign(
            @Valid @RequestBody CreateVaccinationCampaignRequestDto requestDto) {
        VaccinationCampaignResponseDto campaign = vaccinationService.createVaccinationCampaign(requestDto);
        return new ResponseEntity<>(campaign, HttpStatus.CREATED);
    }

    @PutMapping("/campaigns/{campaignId}")
    @PreAuthorize("hasAnyRole('SchoolAdmin ', 'StaffManager')")
    @Operation(
            summary = "Cập nhật chiến dịch tiêm chủng (chỉ khi ở trạng thái DRAFT)",
            description = """
                    ### Mô tả
                    Cập nhật thông tin chiến dịch tiêm chủng.
                    - **Điều kiện:** Chỉ có thể cập nhật khi chiến dịch đang ở trạng thái `DRAFT`.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc không thể cập nhật", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> updateVaccinationCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody CreateVaccinationCampaignRequestDto requestDto) {
        VaccinationCampaignResponseDto updatedCampaign = vaccinationService.updateVaccinationCampaign(campaignId, requestDto);
        return ResponseEntity.ok(updatedCampaign);
    }

    @GetMapping("/campaigns/{campaignId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Lấy thông tin chiến dịch tiêm chủng theo ID",
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của một chiến dịch tiêm chủng cụ thể.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> getVaccinationCampaignById(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.getVaccinationCampaignById(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @GetMapping("/campaigns")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Lấy danh sách tất cả chiến dịch tiêm chủng có lọc và phân trang",
            description = """
                    ### Mô tả
                    Lấy danh sách chiến dịch tiêm chủng với khả năng lọc và phân trang.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
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
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @Operation(
            summary = "Lên lịch chiến dịch (DRAFT -> SCHEDULED)",
            description = """
                    ### Mô tả
                    Chuyển trạng thái chiến dịch từ `DRAFT` sang `SCHEDULED`. Sau khi lên lịch, không thể chỉnh sửa thông tin cơ bản nữa.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    - **Thông báo:** Gửi thông báo đến phụ huynh về chiến dịch tiêm chủng sắp tới.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lên lịch thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Không thể lên lịch", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> scheduleCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.scheduleCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/start")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @Operation(
            summary = "Bắt đầu chiến dịch (PREPARING -> IN_PROGRESS)",
            description = """
                    ### Mô tả
                    Bắt đầu thực hiện chiến dịch, chuyển trạng thái từ `PREPARING` sang `IN_PROGRESS`.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bắt đầu thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Không thể bắt đầu", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> startCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.startCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/complete")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @Operation(
            summary = "Hoàn thành chiến dịch (IN_PROGRESS -> COMPLETED)",
            description = """
                    ### Mô tả
                    Đánh dấu chiến dịch đã hoàn thành, chuyển trạng thái từ `IN_PROGRESS` sang `COMPLETED`.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hoàn thành thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Không thể hoàn thành", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> completeCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.completeCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/cancel")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @Operation(
            summary = "Hủy chiến dịch tiêm chủng",
            description = """
                    ### Mô tả
                    Hủy bỏ một chiến dịch tiêm chủng (trừ khi đã `COMPLETED`).
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    - **Thông báo:** Gửi thông báo đến phụ huynh về việc hủy chiến dịch.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hủy thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Không thể hủy", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> cancelCampaign(@PathVariable Long campaignId) {
        VaccinationCampaignResponseDto campaign = vaccinationService.cancelCampaign(campaignId);
        return ResponseEntity.ok(campaign);
    }

    @PostMapping("/campaigns/{campaignId}/reschedule")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @Operation(
            summary = "Thay đổi lịch chiến dịch (chỉ khi ở trạng thái PREPARING)",
            description = """
                    ### Mô tả
                    Thay đổi thời gian thực hiện chiến dịch.
                    - **Điều kiện:** Chỉ có thể thực hiện khi chiến dịch đang ở trạng thái `PREPARING`.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    - **Thông báo:** Gửi thông báo đến phụ huynh về lịch tiêm chủng mới.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thay đổi lịch thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationCampaignResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Không thể thay đổi lịch", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
    public ResponseEntity<VaccinationCampaignResponseDto> rescheduleCampaign(
            @PathVariable Long campaignId,
            @Valid @RequestBody RescheduleCampaignRequestDto requestDto) {
        VaccinationCampaignResponseDto campaign = vaccinationService.rescheduleCampaign(campaignId, requestDto);
        return ResponseEntity.ok(campaign);
    }

    // --- Consent Endpoints ---

    @GetMapping("/campaigns/{campaignId}/consents")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    @Operation(
            description = """
                    ### Mô tả
                    Lấy danh sách tất cả phiếu đồng ý tiêm chủng của học sinh trong một chiến dịch.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `StaffManager`, hoặc `MedicalStaff`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chiến dịch", content = @Content)
    })
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
    @Operation(
            summary = "Lấy thông tin phiếu đồng ý theo ID",
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của một phiếu đồng ý tiêm chủng.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực. Service sẽ kiểm tra quyền truy cập chi tiết.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationConsentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phiếu đồng ý", content = @Content)
    })
    public ResponseEntity<VaccinationConsentResponseDto> getConsentById(@PathVariable Long consentId) {
        VaccinationConsentResponseDto consent = vaccinationService.getConsentById(consentId);
        return ResponseEntity.ok(consent);
    }

    @PutMapping("/consents/{consentId}")
    @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")
    @Operation(
            summary = "Cập nhật phản hồi phiếu đồng ý (bởi nhân viên)",
            description = """
                    ### Mô tả
                    Cập nhật trạng thái và thông tin phản hồi của phiếu đồng ý tiêm chủng.
                    - **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(mediaType = "application/json", schema = @Schema(implementation = VaccinationConsentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy phiếu đồng ý", content = @Content)
    })
    public ResponseEntity<VaccinationConsentResponseDto> updateConsentResponse(
            @PathVariable Long consentId,
            @Valid @RequestBody UpdateVaccinationConsentRequestDto requestDto) {
        VaccinationConsentResponseDto updatedConsent = vaccinationService.updateConsentResponse(consentId, requestDto);
        return ResponseEntity.ok(updatedConsent);
    }
}
