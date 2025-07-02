package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.CreateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.UpdateStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
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
import org.apache.commons.lang3.RandomStringUtils;
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
    private final NotificationService notificationService;

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

    @Transactional
    public StudentDto updateStudent(Long studentId, UpdateStudentRequestDto dto) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        log.info("Yêu cầu cập nhật thông tin cho học sinh ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> {
                    log.warn("Không tìm thấy học sinh với ID: {}", studentId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy học sinh với ID: " + studentId);
                });

        // Cập nhật các trường thông tin từ DTO
            studentMapper.updateStudentFromDto(dto, student);

        Student updatedStudent = studentRepository.save(student);

        // Gửi thông báo cho phụ huynh
        notifyParents(updatedStudent,
            String.format("Thông tin của con bạn, %s, vừa được cập nhật.", updatedStudent.getFullName()),
            "/profile/student/" + updatedStudent.getId(),
            currentUser);

        log.info("Đã cập nhật thành công thông tin cho học sinh: {}", updatedStudent.getFullName());
        return studentMapper.studentToStudentDto(updatedStudent);
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
    public StudentDto graduateStudent(Long studentId) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        log.info("Yêu cầu đánh dấu tốt nghiệp cho học sinh ID: {}", studentId);
        Student student = findStudentByIdAndCheckStatus(studentId, StudentStatus.ACTIVE, "Chỉ học sinh đang hoạt động mới có thể được đánh dấu tốt nghiệp.");
        student.setStatus(StudentStatus.GRADUATED);
        Student savedStudent = studentRepository.save(student);

        // Gửi thông báo cho phụ huynh
        notifyParents(savedStudent,
            String.format("Con của bạn, %s, đã được đánh dấu là đã tốt nghiệp.", savedStudent.getFullName()),
            "/profile/student/" + savedStudent.getId(),
            currentUser);

        log.info("Đã đánh dấu tốt nghiệp thành công cho học sinh: {}", savedStudent.getFullName());
        return studentMapper.studentToStudentDto(savedStudent);
    }

    @Transactional
    public StudentDto withdrawStudent(Long studentId) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        log.info("Yêu cầu đánh dấu thôi học cho học sinh ID: {}", studentId);
        Student student = findStudentByIdAndCheckStatus(studentId, StudentStatus.ACTIVE, "Chỉ học sinh đang hoạt động mới có thể được đánh dấu thôi học.");
        student.setStatus(StudentStatus.WITHDRAWN);
        Student savedStudent = studentRepository.save(student);

        // Gửi thông báo cho phụ huynh
        notifyParents(savedStudent,
            String.format("Con của bạn, %s, đã được đánh dấu là đã thôi học.", savedStudent.getFullName()),
            "/profile/student/" + savedStudent.getId(),
            currentUser);

        log.info("Đã đánh dấu thôi học thành công cho học sinh: {}", savedStudent.getFullName());
        return studentMapper.studentToStudentDto(savedStudent);
    }

    @Transactional
    public StudentDto reactivateStudent(Long studentId) {
        User currentUser = userService.getCurrentAuthenticatedUser();
        log.info("Yêu cầu kích hoạt lại cho học sinh ID: {}", studentId);
        Student student = findStudentByIdAndCheckStatus(studentId, StudentStatus.WITHDRAWN, "Chỉ học sinh đã thôi học mới có thể được kích hoạt lại.");
        student.setStatus(StudentStatus.ACTIVE);
        Student savedStudent = studentRepository.save(student);

        // Gửi thông báo cho phụ huynh
        notifyParents(savedStudent,
            String.format("Học sinh %s đã được kích hoạt lại trạng thái học tập tại trường.", savedStudent.getFullName()),
            "/profile/student/" + savedStudent.getId(),
            currentUser);

        log.info("Đã kích hoạt lại thành công cho học sinh: {}", savedStudent.getFullName());
        return studentMapper.studentToStudentDto(savedStudent);
    }

    private void notifyParents(Student student, String content, String link, User sender) {
        String senderEmail = (sender != null) ? sender.getEmail() : "system";
        for (ParentStudentLink parentLink : student.getParentLinks()) {
            User parent = parentLink.getParent();
            if (parent != null && parent.getEmail() != null) {
                notificationService.createAndSendNotification(parent.getEmail(), content, link, senderEmail);
            }
        }
    }

    private Student findStudentByIdAndCheckStatus(Long studentId, StudentStatus expectedStatus, String errorMessage) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy học sinh với ID: " + studentId));

        if (student.getStatus() != expectedStatus) {
            log.warn("Thao tác không hợp lệ cho học sinh ID: {}. Trạng thái hiện tại: {}, Trạng thái yêu cầu: {}", studentId, student.getStatus(), expectedStatus);
            throw new AppException(HttpStatus.CONFLICT, errorMessage);
        }
        return student;
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

