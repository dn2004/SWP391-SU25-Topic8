package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final UserService userService;
    public void authorizeParentAction(User parent, Student student, String action) {
        log.info("Kiểm tra quyền của Phụ huynh {} cho hành động '{}' với học sinh ID {}", parent.getEmail(), action, student.getId());
        boolean isLinked = parentStudentLinkRepository.existsByParentAndStudentAndStatus(parent, student, LinkStatus.ACTIVE);
        if (!isLinked) {
            log.error("Phụ huynh {} không có quyền thực hiện '{}' cho học sinh ID {}.",
                    parent.getEmail(), action, student.getId());
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này cho học sinh được chỉ định.");
        }
        log.info("Phụ huynh {} được xác nhận có liên kết với học sinh ID {}.", parent.getEmail(), student.getId());
    }

    public User getCurrentUserAndValidate() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            log.error("Không thể xác định người dùng hiện tại.");
            throw new AccessDeniedException("Không thể xác thực người dùng hiện tại.");
        }
        log.debug("Người dùng hiện tại: {} (ID: {}, Role: {})", currentUser.getEmail(), currentUser.getUserId(), currentUser.getRole());
        return currentUser;
    }

    public Optional<User> tryGetCurrentUser() {
        try {
            return Optional.of(userService.getCurrentAuthenticatedUser());
        } catch (Exception e) {
            log.debug("Không có người dùng nào được xác thực hoặc đã xảy ra lỗi: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
