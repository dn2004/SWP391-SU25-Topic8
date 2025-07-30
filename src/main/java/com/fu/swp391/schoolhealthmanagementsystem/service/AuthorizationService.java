package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final UserService userService;
    /**
     * Kiểm tra quyền của phụ huynh đối với học sinh cho một hành động cụ thể
     * Ném AppException nếu không có quyền
     */
    public void authorizeParentAction(User parent, Student student, String action) {
        log.info("[AUTH] Kiểm tra quyền của Phụ huynh {} cho hành động '{}' với học sinh ID {}", parent.getEmail(), action, student.getId());
        boolean isLinked = parentStudentLinkRepository.existsByParentAndStudentAndStatus(parent, student, LinkStatus.ACTIVE);
        if (!isLinked) {
            log.warn("[AUTH] Phụ huynh {} không có quyền thực hiện '{}' cho học sinh ID {}.", parent.getEmail(), action, student.getId());
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện hành động này cho học sinh được chỉ định.");
        }
        log.info("[AUTH] Phụ huynh {} được xác nhận có liên kết với học sinh ID {}.", parent.getEmail(), student.getId());
    }

    /**
     * Lấy người dùng hiện tại đã xác thực, ném AppException nếu không xác thực
     */
    public User getCurrentUserAndValidate() {
        User currentUser = userService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            log.error("[AUTH] Không thể xác định người dùng hiện tại.");
            throw new AppException(HttpStatus.UNAUTHORIZED, "Không thể xác thực người dùng hiện tại.");
        }
        log.debug("[AUTH] Người dùng hiện tại: {} (ID: {}, Role: {})", currentUser.getEmail(), currentUser.getUserId(), currentUser.getRole());
        return currentUser;
    }

    /**
     * Thử lấy người dùng hiện tại, trả về Optional rỗng nếu không xác thực
     */
    public Optional<User> tryGetCurrentUser() {
        try {
            return Optional.ofNullable(userService.getCurrentAuthenticatedUser());
        } catch (Exception e) {
            log.debug("[AUTH] Không có người dùng nào được xác thực hoặc đã xảy ra lỗi: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
