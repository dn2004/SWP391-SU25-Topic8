package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.parent.LinkStudentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.StudentMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentStudentLinkService {

    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final StudentMapper studentMapper;

    @Transactional
    public void linkParentToStudentByInvitation(LinkStudentRequestDto dto) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User parent = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new BadCredentialsException("Không tìm thấy người dùng với email: " + currentUserEmail));

        log.info("Phụ huynh {} đang cố gắng liên kết với học sinh bằng mã mời: {}", parent.getEmail(), dto.invitationCode());

        Student student = studentRepository.findByInvitationCode(dto.invitationCode())
                .orElseThrow(() -> {
                    log.warn("Mã mời {} không hợp lệ hoặc không tìm thấy học sinh.", dto.invitationCode());
                    return new ResourceNotFoundException("Mã mời không hợp lệ hoặc không tìm thấy học sinh.");
                });

        // Kiểm tra xem phụ huynh này đã liên kết với học sinh này chưa
        boolean alreadyLinked = parentStudentLinkRepository.existsByParentAndStudent(parent, student);
        if (alreadyLinked) {
            log.info("Phụ huynh {} đã liên kết với học sinh {} (Mã: {}) rồi.", parent.getEmail(), student.getFullName(), student.getStudentCode());

            ParentStudentLink existingLink = parentStudentLinkRepository.findByParentAndStudent(parent, student)
                    .orElseThrow(() -> new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Liên kết tồn tại nhưng không thể truy xuất."));
            if(existingLink.getStatus() == LinkStatus.ACTIVE){
                throw new AppException(HttpStatus.CONFLICT, "Bạn đã liên kết với học sinh này rồi.");
            }
        }


        ParentStudentLink link = new ParentStudentLink();
        link.setParent(parent);
        link.setStudent(student);
        link.setRelationshipType(dto.relationshipType());
        link.setStatus(LinkStatus.ACTIVE);

        parentStudentLinkRepository.save(link);
        log.info("Phụ huynh {} đã liên kết thành công với học sinh {} (Mã: {}) với vai trò {}. Trạng thái: ACTIVE",
                parent.getEmail(), student.getFullName(), student.getStudentCode(), dto.relationshipType());
    }

    @Transactional(readOnly = true)
    public Page<StudentDto> getMyLinkedStudents(Pageable pageable) {
        User currentParent = userService.getCurrentAuthenticatedUser();

        // Kiểm tra này có thể không cần thiết nếu ParentController đã có @PreAuthorize("hasRole('Parent')")
        // Tuy nhiên, để an toàn ở tầng service thì vẫn tốt.
        if (currentParent.getRole() != UserRole.Parent) {
            log.warn("Người dùng {} (vai trò {}) không phải là Phụ huynh, cố gắng lấy danh sách học sinh liên kết.",
                    currentParent.getEmail(), currentParent.getRole());
            throw new AppException(HttpStatus.FORBIDDEN, "Chức năng này chỉ dành cho phụ huynh.");
        }

        log.info("Phụ huynh {} yêu cầu danh sách học sinh đã liên kết - Trang: {}, Kích thước: {}",
                currentParent.getEmail(), pageable.getPageNumber(), pageable.getPageSize());

        Page<Student> linkedStudentsPage = parentStudentLinkRepository.findStudentsByParent(currentParent, pageable);

        return linkedStudentsPage.map(studentMapper::studentToStudentDto);
    }
}