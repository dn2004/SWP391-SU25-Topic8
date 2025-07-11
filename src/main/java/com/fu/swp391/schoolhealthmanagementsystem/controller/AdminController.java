package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.admin.CreateStaffRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.admin.UserActivationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.CreateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.service.AdminService;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentService;
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
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - User Management", description = "APIs quản lý người dùng cho Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SchoolAdmin')")
@Slf4j
public class AdminController {
    private final AdminService adminService;
    private final StudentService studentService;

    @PostMapping("/staff")
    @Operation(
            summary = "Tạo tài khoản nhân viên mới (MedicalStaff, StaffManager)",
            description = """
                    ### Mô tả
                    Tạo tài khoản nhân viên mới với vai trò `MedicalStaff` hoặc `StaffManager`.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    - **Thông báo:** Mật khẩu ngẫu nhiên sẽ được tạo và gửi đến email của nhân viên mới.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo tài khoản thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "409", description = "Email đã tồn tại", content = @Content)
    })
    public ResponseEntity<UserDto> createStaffAccount(@Valid @RequestBody CreateStaffRequestDto requestDto) {
        log.info("API Admin: Yêu cầu tạo tài khoản nhân viên - email: {}, role: {}", requestDto.email(), requestDto.role());
        UserDto createdStaff = adminService.createStaffAccount(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStaff);
    }

    @PutMapping("/{userId}/activation")
    @Operation(
            summary = "Kích hoạt hoặc vô hiệu hóa tài khoản người dùng",
            description = """
                    ### Mô tả
                    Cập nhật trạng thái kích hoạt (active/inactive) cho một tài khoản người dùng.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    - **Thông báo:** Gửi thông báo đến người dùng bị ảnh hưởng về việc tài khoản của họ đã được kích hoạt hoặc vô hiệu hóa.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content)
    })
    public ResponseEntity<UserDto> updateUserActivationStatus(
            @Parameter(description = "ID của người dùng cần cập nhật") @PathVariable Long userId,
            @Valid @RequestBody UserActivationRequestDto requestDto) {
        log.info("API Admin: Yêu cầu cập nhật trạng thái kích hoạt cho user ID {} thành {}", userId, requestDto.isActive());
        UserDto updatedUser = adminService.updateUserActivationStatus(userId, requestDto.isActive());
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Tìm kiếm và lấy danh sách phụ huynh (phân trang)",
            description = """
                    ### Mô tả
                    Lấy danh sách phụ huynh có phân trang và hỗ trợ lọc theo nhiều tiêu chí.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    """
    )
    @GetMapping("/parents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<Page<UserDto>> getParents(
            @Parameter(description = "Tên phụ huynh")
            @RequestParam(required = false) String fullName,

            @Parameter(description = "Email phụ huynh")
            @RequestParam(required = false) String email,

            @Parameter(description = "Số điện thoại")
            @RequestParam(required = false) String phone,

            @Parameter(description = "Trạng thái kích hoạt")
            @RequestParam(required = false) Boolean active,

            @ParameterObject
            @PageableDefault(size = 10, page = 0, sort = "fullName") Pageable pageable
    ) {
        log.info("API Admin: Tìm kiếm phụ huynh - fullName: {}, email: {}, phone: {}, active: {}, page: {}, size: {}",
                fullName, email, phone, active, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserDto> parents = adminService.searchUsersByRole(UserRole.Parent, fullName, email, phone, active, pageable);
        return ResponseEntity.ok(parents);
    }

    @Operation(
            summary = "Tìm kiếm và lấy danh sách nhân viên y tế (phân trang)",
            description = """
                    ### Mô tả
                    Lấy danh sách nhân viên y tế có phân trang và hỗ trợ lọc theo nhiều tiêu chí.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    """
    )
    @GetMapping("/medical-staff")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<Page<UserDto>> getMedicalStaff(
            @Parameter(description = "Tên nhân viên y tế")
            @RequestParam(required = false) String fullName,

            @Parameter(description = "Email nhân viên y tế")
            @RequestParam(required = false) String email,

            @Parameter(description = "Số điện thoại")
            @RequestParam(required = false) String phone,

            @Parameter(description = "Trạng thái kích hoạt")
            @RequestParam(required = false) Boolean active,

            @ParameterObject
            @PageableDefault(size = 10, page = 0, sort = "fullName") Pageable pageable
    ) {
        log.info("API Admin: Tìm kiếm nhân viên y tế - fullName: {}, email: {}, phone: {}, active: {}, page: {}, size: {}",
                fullName, email, phone, active, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserDto> medicalStaff = adminService.searchUsersByRole(UserRole.MedicalStaff, fullName, email, phone, active, pageable);
        return ResponseEntity.ok(medicalStaff);
    }

    @Operation(
            summary = "Tìm kiếm và lấy danh sách quản lý y tế (phân trang)",
            description = """
                    ### Mô tả
                    Lấy danh sách quản lý y tế có phân trang và hỗ trợ lọc theo nhiều tiêu chí.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    """
    )
    @GetMapping("/staff-managers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<Page<UserDto>> getStaffManagers(
            @Parameter(description = "Tên quản lý y tế")
            @RequestParam(required = false) String fullName,

            @Parameter(description = "Email quản lý y tế")
            @RequestParam(required = false) String email,

            @Parameter(description = "Số điện thoại")
            @RequestParam(required = false) String phone,

            @Parameter(description = "Trạng thái kích hoạt")
            @RequestParam(required = false) Boolean active,

            @ParameterObject
            @PageableDefault(size = 10, page = 0, sort = "fullName") Pageable pageable
    ) {
        log.info("API Admin: Tìm kiếm quản lý y tế - fullName: {}, email: {}, phone: {}, active: {}, page: {}, size: {}",
                fullName, email, phone, active, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserDto> staffManagers = adminService.searchUsersByRole(UserRole.StaffManager, fullName, email, phone, active, pageable);
        return ResponseEntity.ok(staffManagers);
    }

    @Operation(
            summary = "Admin tạo hồ sơ học sinh mới",
            description = """
                    ### Mô tả
                    Tạo một hồ sơ học sinh mới trong hệ thống.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    """
    )
    @PostMapping("/students")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo hồ sơ học sinh thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "409", description = "Học sinh đã tồn tại", content = @Content)
    })
    public ResponseEntity<StudentDto> createStudentProfile(@Valid @RequestBody CreateStudentRequestDto requestDto) {
        StudentDto createdStudent = studentService.createStudent(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @Operation(
            summary = "Admin lấy thông tin chi tiết một người dùng bằng ID",
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của bất kỳ người dùng nào trong hệ thống bằng ID của họ.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`.
                    """
    )
    @GetMapping("/{userId}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng", content = @Content)
    })
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID của người dùng cần truy vấn") @PathVariable Long userId) {
        log.info("API Admin: Yêu cầu lấy thông tin user ID: {}", userId);
        UserDto userDto = adminService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }
}