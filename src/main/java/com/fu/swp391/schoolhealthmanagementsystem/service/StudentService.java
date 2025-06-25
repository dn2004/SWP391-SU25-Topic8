package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.CreateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.StudentMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.StudentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils; // Cần dependency commons-lang3
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final ParentStudentLinkRepository parentStudentLinkRepository; // Inject
    private final UserService userService;
    private final StudentSpecification studentSpecification;

    @Transactional
    public StudentDto createStudent(CreateStudentRequestDto dto) {
        Student student = studentMapper.createStudentRequestDtoToStudent(dto);

        // Tạo invitation code duy nhất
        String invitationCode;
        do {
            invitationCode = RandomStringUtils.random(10, true, true).toUpperCase();
        } while (studentRepository.findByInvitationCode(invitationCode).isPresent());
        student.setInvitationCode(invitationCode);
        student.setStatus(StudentStatus.ACTIVE); // Đảm bảo active khi tạo

        Student savedStudent = studentRepository.save(student);
        log.info("Đã tạo thành công học sinh: {} - Mã mời: {}", savedStudent.getFullName(), savedStudent.getInvitationCode());
        return studentMapper.studentToStudentDto(savedStudent);
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long studentId) {
        log.info("Yêu cầu lấy thông tin học sinh với ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy học sinh với ID: {}", studentId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy học sinh với ID: " + studentId);
                });

        // Kiểm tra quyền truy cập (ví dụ cho phụ huynh)
        User currentParent = userService.getCurrentAuthenticatedUser();
        if (currentParent != null) {
            // Nếu là Admin hoặc StaffManager, MedicalStaff (tùy theo quyền bạn muốn cấp) -> cho phép
            if (currentParent.getRole() == UserRole.SchoolAdmin ||
                    currentParent.getRole() == UserRole.StaffManager ||
                    currentParent.getRole() == UserRole.MedicalStaff) {
                log.info("Admin/Nhân viên {} truy cập thông tin học sinh ID: {}", currentParent.getEmail(), studentId);
                return studentMapper.studentToStudentDto(student);
            }
            // Nếu là Phụ huynh, kiểm tra xem có liên kết với học sinh này không
            if (currentParent.getRole() == UserRole.Parent) {
                boolean isLinked = parentStudentLinkRepository.existsByParentAndStudent(currentParent, student);
                if (isLinked) {
                    log.info("Phụ huynh {} truy cập thông tin học sinh ID: {} (đã liên kết)", currentParent.getEmail(), studentId);
                    return studentMapper.studentToStudentDto(student);
                } else {
                    log.warn("Phụ huynh {} cố gắng truy cập thông tin học sinh ID: {} mà không có liên kết.", currentParent.getEmail(), studentId);
                    throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem thông tin học sinh này.");
                }
            }
        }
        // Nếu không rơi vào các trường hợp trên, hoặc không xác thực -> từ chối
        log.warn("Truy cập không được phép vào thông tin học sinh ID: {}", studentId);
        throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem thông tin học sinh này.");
    }

    @Transactional(readOnly = true)
    public Page<StudentDto> getAllStudents(String fullName, String className, StudentStatus status, Pageable pageable) {
        User currentUser = userService.getCurrentAuthenticatedUser(); // Lấy user từ service đã có

        if (!(currentUser.getRole() == UserRole.SchoolAdmin ||
                currentUser.getRole() == UserRole.StaffManager ||
                currentUser.getRole() == UserRole.MedicalStaff)) {
            throw new AppException(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.");
        }

        Specification<Student> spec = studentSpecification.hasFullName(fullName)
                .and(studentSpecification.hasClassName(className))
                .and(studentSpecification.hasStatus(status));

        Page<Student> studentPage = studentRepository.findAll(spec, pageable);
        return studentPage.map(studentMapper::studentToStudentDto);
    }

    @Transactional
    public boolean deleteStudent(Long studentId) {
        log.info("Yêu cầu xóa học sinh với ID: {}", studentId);

        // Kiểm tra quyền hạn - chỉ admin hoặc staff manager có thể xóa học sinh
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (!(currentUser.getRole() == UserRole.SchoolAdmin ||
                currentUser.getRole() == UserRole.StaffManager)) {
            log.warn("Người dùng {} không có quyền xóa học sinh", currentUser.getEmail());
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa học sinh");
        }

        // Tìm học sinh theo ID
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy học sinh với ID: {}", studentId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy học sinh với ID: " + studentId);
                });

        // Kiểm tra xem học sinh có phụ huynh liên kết không
        boolean hasParentLinks = parentStudentLinkRepository.existsByStudent(student);
        if (hasParentLinks) {
            log.warn("Không thể xóa học sinh ID: {} vì đã có phụ huynh liên kết", studentId);
            throw new AppException(HttpStatus.CONFLICT,
                    "Không thể xóa học sinh này vì đã có phụ huynh liên kết. Hãy gỡ bỏ tất cả liên kết phụ huynh trước khi xóa.");
        }

        // Kiểm tra xem học sinh có sự cố sức khỏe liên quan không
        if (!student.getHealthIncidents().isEmpty()) {
            log.warn("Không thể xóa học sinh ID: {} vì đã có {} sự cố sức khỏe liên quan",
                    studentId, student.getHealthIncidents().size());
            throw new AppException(HttpStatus.CONFLICT,
                    "Không thể xóa học sinh này vì đã có sự cố sức khỏe liên quan. Hãy đặt trạng thái thành không hoạt động thay vì xóa.");
        }

        // Kiểm tra xem học sinh có thông tin tiêm chủng không
        if (!student.getVaccinations().isEmpty()) {
            log.warn("Không thể xóa học sinh ID: {} vì đã có {} thông tin tiêm chủng",
                    studentId, student.getVaccinations().size());
            throw new AppException(HttpStatus.CONFLICT,
                    "Không thể xóa học sinh này vì đã có thông tin tiêm chủng. Hãy đặt trạng thái thành không hoạt động thay vì xóa.");
        }

        // Nếu tất cả điều kiện đều thỏa mãn, tiến hành xóa học sinh
        try {
            studentRepository.delete(student);
            log.info("Đã xóa thành công học sinh với ID: {}", studentId);
            return true;
        } catch (Exception ex) {
            log.error("Lỗi khi xóa học sinh ID: {}", studentId, ex);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi xóa học sinh: " + ex.getMessage());
        }
    }
}