package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard.DashboardAdminDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard.DashboardMedicalStaffDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard.DashboardStaffManagerDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.DashboardAdminService;
import com.fu.swp391.schoolhealthmanagementsystem.service.DashboardMedicalStaffService;
import com.fu.swp391.schoolhealthmanagementsystem.service.DashboardStaffManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Analytics", description = "APIs thống kê dashboard cho các vai trò khác nhau")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    private final DashboardAdminService dashboardAdminService;
    private final DashboardStaffManagerService dashboardStaffManagerService;
    private final DashboardMedicalStaffService dashboardMedicalStaffService;


    @Operation(
            summary = "Lấy thống kê dashboard cho Staff Manager",
            description = """
                    ### Mô tả
                    Trả về các số liệu tổng hợp cho dashboard của quản lý nhân viên y tế.
                    - **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardStaffManagerDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasRole('StaffManager') or hasRole('SchoolAdmin')")
    @GetMapping("/staff-manager")
    public ResponseEntity<DashboardStaffManagerDto> getStaffManagerDashboard() {
        DashboardStaffManagerDto dto = dashboardStaffManagerService.getStaffManagerDashboard();
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Lấy thống kê dashboard cho admin",
            description = """
                    ### Mô tả
                    Trả về các số liệu tổng hợp cho dashboard của quản trị viên trường học.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardAdminDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasRole('SchoolAdmin') or hasRole('StaffManager')")
    @GetMapping("/admin")
    public ResponseEntity<DashboardAdminDto> getAdminDashboard() {
        DashboardAdminDto dto = dashboardAdminService.getAdminDashboard();
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "Lấy thống kê dashboard cho Medical Staff",
            description = """
                    ### Mô tả
                    Trả về các số liệu tổng hợp cho dashboard của nhân viên y tế.
                    - **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DashboardMedicalStaffDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasRole('MedicalStaff') or hasRole('StaffManager') or hasRole('SchoolAdmin')")
    @GetMapping("/medical-staff")
    public ResponseEntity<DashboardMedicalStaffDto> getMedicalStaffDashboard() {
        DashboardMedicalStaffDto dto = dashboardMedicalStaffService.getMedicalStaffDashboard();
        return ResponseEntity.ok(dto);
    }
}
