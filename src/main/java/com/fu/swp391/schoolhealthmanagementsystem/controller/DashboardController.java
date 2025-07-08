package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.DashboardAdminDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.DashboardMedicalStaffDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.DashboardStaffManagerDto;
import com.fu.swp391.schoolhealthmanagementsystem.service.DashboardAdminService;
import com.fu.swp391.schoolhealthmanagementsystem.service.DashboardMedicalStaffService;
import com.fu.swp391.schoolhealthmanagementsystem.service.DashboardStaffManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardAdminService dashboardAdminService;
    private final DashboardStaffManagerService dashboardStaffManagerService;
    private final DashboardMedicalStaffService dashboardMedicalStaffService;


    @Operation(summary = "Lấy thống kê dashboard cho Staff Manager", description = "Trả về các số liệu tổng hợp cho dashboard quản lý nhân sự.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('StaffManager') or hasRole('SchoolAdmin')")
    @GetMapping("/staff-manager")
    public ResponseEntity<DashboardStaffManagerDto> getStaffManagerDashboard() {
        DashboardStaffManagerDto dto = dashboardStaffManagerService.getStaffManagerDashboard();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Lấy thống kê dashboard cho admin", description = "Trả về các số liệu tổng hợp cho dashboard admin.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SchoolAdmin') or hasRole('StaffManager')")
    @GetMapping("/admin")
    public ResponseEntity<DashboardAdminDto> getAdminDashboard() {
        DashboardAdminDto dto = dashboardAdminService.getAdminDashboard();
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Lấy thống kê dashboard cho Medical Staff", description = "Trả về các số liệu tổng hợp cho dashboard nhân viên y tế.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('MedicalStaff') or hasRole('StaffManager') or hasRole('SchoolAdmin')")
    @GetMapping("/medical-staff")
    public ResponseEntity<DashboardMedicalStaffDto> getMedicalStaffDashboard() {
        DashboardMedicalStaffDto dto = dashboardMedicalStaffService.getMedicalStaffDashboard();
        return ResponseEntity.ok(dto);
    }
}
