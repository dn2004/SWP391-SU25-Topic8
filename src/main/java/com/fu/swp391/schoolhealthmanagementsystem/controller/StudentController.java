package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.UpdateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Gender;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students") // Hoặc một base path chung hơn
@RequiredArgsConstructor
@Tag(name = "Student Management", description = "APIs quản lý học sinh")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class StudentController {

    private final StudentService studentService;


    @GetMapping("/{studentId}")
    @Operation(summary = "Lấy thông tin chi tiết một học sinh bằng ID",
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của một học sinh.
                    - **Phân quyền:**
                        - `Parent`: Chỉ xem được thông tin của con mình.
                        - `MedicalStaff`, `StaffManager`, `SchoolAdmin`: Có thể xem của bất kỳ học sinh nào.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    // Cho phép Admin, Staff, và Parent (service sẽ kiểm tra parent có link không)
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff', 'Parent')")
    public ResponseEntity<StudentDto> getStudentById(
            @Parameter(description = "ID của học sinh cần truy vấn") @PathVariable Long studentId) {
        log.info("API: Yêu cầu lấy thông tin học sinh ID: {}", studentId);
        StudentDto studentDto = studentService.getStudentById(studentId);
        return ResponseEntity.ok(studentDto);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách học sinh (phân trang và lọc)",
            description = """
                    ### Mô tả
                    Lấy danh sách học sinh trong toàn trường với các bộ lọc và phân trang.
                    - **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<Page<StudentDto>> getAllStudents(
            @Parameter(description = "Tìm kiếm tổng quát (tên hoặc ID)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Khối lớp (MAM, CHOI, LA)")
            @RequestParam(required = false) ClassGroup classGroup,

            @Parameter(description = "Lớp (A, B, C, D...)")
            @RequestParam(required = false) Class classValue,

            @Parameter(description = "Trạng thái")
            @RequestParam(required = false) StudentStatus status,

            @Parameter(description = "Giới tính")
            @RequestParam(required = false) Gender gender,

            @Parameter(description = "Ngày sinh chính xác (định dạng: yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,

            @ParameterObject
            @PageableDefault(size = 10, sort = "fullName") Pageable pageable) {

        log.info("API: Yêu cầu lấy danh sách học sinh - search: {}, classGroup: {}, classValue: {}, status: {}, gender: {},dateOfBirth: {}, page: {}, size: {}",
                search, classGroup, classValue, status, gender,dateOfBirth, pageable.getPageNumber(), pageable.getPageSize());

        Page<StudentDto> studentPage = studentService.getAllStudents(search, classGroup, classValue, status, gender, dateOfBirth, pageable);
        return ResponseEntity.ok(studentPage);
    }

    @GetMapping("/by-class-group-and-class/{classGroup}/{classValue}")
    @Operation(summary = "Lấy danh sách học sinh theo khối lớp và lớp cụ thể",
            description = """
                    ### Mô tả
                    Lấy danh sách tất cả học sinh thuộc một lớp và khối lớp cụ thể.
                    - **Phân quyền:** Yêu cầu vai trò `MedicalStaff`, `StaffManager`, hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    public ResponseEntity<List<StudentDto>> getStudentsByClassGroupAndClass(
            @Parameter(description = "Khối lớp (MAM, CHOI, LA)") @PathVariable ClassGroup classGroup,
            @Parameter(description = "Lớp (A, B, C, D...)") @PathVariable Class classValue) {

        log.info("API: Yêu cầu lấy danh sách học sinh theo khối {} và lớp {}", classGroup, classValue);
        List<StudentDto> students = studentService.findStudentsByClassGroupAndClass(classGroup, classValue);
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "Cập nhật thông tin học sinh",
            description = """
                    ### Mô tả
                    Cập nhật thông tin hồ sơ của một học sinh.
                    - **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    public ResponseEntity<StudentDto> updateStudent(
            @Parameter(description = "ID của học sinh cần cập nhật") @PathVariable Long studentId,
            @Valid @RequestBody UpdateStudentRequestDto updateStudentRequestDto) {
        log.info("API: Yêu cầu cập nhật thông tin cho học sinh ID: {}", studentId);
        StudentDto updatedStudent = studentService.updateStudent(studentId, updateStudentRequestDto);
        return ResponseEntity.ok(updatedStudent);
    }

    @PostMapping("/{studentId}/graduate")
    @Operation(summary = "Đánh dấu học sinh đã tốt nghiệp",
            description = """
                    ### Mô tả
                    Cập nhật trạng thái của học sinh thành `GRADUATED`.
                    - **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    public ResponseEntity<StudentDto> graduateStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId) {
        log.info("API: Yêu cầu đánh dấu tốt nghiệp cho học sinh ID: {}", studentId);
        StudentDto updatedStudent = studentService.graduateStudent(studentId);
        return ResponseEntity.ok(updatedStudent);
    }

    @PostMapping("/{studentId}/withdraw")
    @Operation(summary = "Đánh dấu học sinh đã thôi học",
            description = """
                    ### Mô tả
                    Cập nhật trạng thái của học sinh thành `WITHDRAWN`.
                    - **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    public ResponseEntity<StudentDto> withdrawStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId) {
        log.info("API: Yêu cầu đánh dấu thôi học cho học sinh ID: {}", studentId);
        StudentDto updatedStudent = studentService.withdrawStudent(studentId);
        return ResponseEntity.ok(updatedStudent);
    }

    @PostMapping("/{studentId}/reactivate")
    @Operation(summary = "Kích hoạt lại học sinh đã thôi học",
            description = """
                    ### Mô tả
                    Cập nhật trạng thái của học sinh từ `WITHDRAWN` trở lại `STUDYING`.
                    - **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StudentDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    public ResponseEntity<StudentDto> reactivateStudent(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId) {
        log.info("API: Yêu cầu kích hoạt lại cho học sinh ID: {}", studentId);
        StudentDto updatedStudent = studentService.reactivateStudent(studentId);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{studentId}")
    @Operation(
            summary = "Xóa học sinh theo ID",
            description = """
                    ### Mô tả
                    Xóa hồ sơ của một học sinh khỏi hệ thống.
                    - **Điều kiện:** Chỉ cho phép xóa khi học sinh chưa có phụ huynh liên kết, chưa có sự cố sức khỏe và chưa có thông tin tiêm chủng.
                    - **Phân quyền:** Yêu cầu vai trò `StaffManager` hoặc `SchoolAdmin`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa học sinh thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa học sinh"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy học sinh"),
            @ApiResponse(responseCode = "409", description = "Không thể xóa vì học sinh đã có dữ liệu liên quan")
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    public ResponseEntity<Map<String, Object>> deleteStudent(
            @Parameter(description = "ID của học sinh cần xóa") @PathVariable Long studentId) {
        log.info("API: Yêu cầu xóa học sinh ID: {}", studentId);
        boolean deleted = studentService.deleteStudent(studentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("message", "Đã xóa học sinh thành công");

        return ResponseEntity.ok(response);
    }
}