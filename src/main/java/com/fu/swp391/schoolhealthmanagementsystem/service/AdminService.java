package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.admin.CreateStaffRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.UserMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.info("[ADMIN] Yêu cầu lấy thông tin người dùng với ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[ADMIN] Không tìm thấy người dùng với ID: {}", userId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + userId);
                });
        return userMapper.userToUserDto(user);
    }

    @Transactional
    public UserDto createStaffAccount(CreateStaffRequestDto dto) {
        log.info("[ADMIN] Tạo tài khoản nhân viên mới cho email: {}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("[ADMIN] Email {} đã tồn tại khi cố tạo tài khoản.", dto.email());
            throw new AppException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng!");
        }
        if (userRepository.existsByPhoneNumber(dto.phoneNumber())) {
            log.warn("[ADMIN] Số điện thoại {} đã tồn tại khi cố tạo tài khoản.", dto.phoneNumber());
            throw new AppException(HttpStatus.BAD_REQUEST, "Số điện thoại đã được sử dụng!");
        }
        if (dto.role() != UserRole.MedicalStaff && dto.role() != UserRole.StaffManager) {
            log.warn("[ADMIN] Cố gắng tạo tài khoản với vai trò không hợp lệ: {}", dto.role());
            throw new AppException(HttpStatus.BAD_REQUEST, "Vai trò nhân viên không hợp lệ.");
        }

        User staff = new User();
        staff.setFullName(dto.fullName());
        staff.setEmail(dto.email());
        staff.setPhoneNumber(dto.phoneNumber());
        staff.setRole(dto.role());
        staff.setActive(true); // Kích hoạt tài khoản ngay khi tạo

        String randomPassword = generateRandomPassword();
        staff.setPassword(passwordEncoder.encode(randomPassword));

        User savedStaff = userRepository.save(staff);
        log.info("[ADMIN] Đã tạo tài khoản nhân viên {} thành công: {}", dto.role(), savedStaff.getEmail());

        // Gửi email thông tin đăng nhập cho nhân viên
        emailService.sendNewStaffCredentialsEmail(savedStaff, randomPassword);

        return userMapper.userToUserDto(savedStaff);
    }

    @Transactional
    public UserDto updateUserActivationStatus(Long userId, boolean isActive) {
        log.info("[ADMIN] Cập nhật trạng thái kích hoạt cho user ID {}: {}", userId, isActive ? "Kích hoạt" : "Vô hiệu hóa");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[ADMIN] Không tìm thấy user ID {} để cập nhật trạng thái.", userId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng.");
                });

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        log.info("[ADMIN] Đã cập nhật trạng thái người dùng ID {} thành {}", userId, isActive ? "active" : "inactive");

        // Gửi thông báo cho người dùng về thay đổi trạng thái tài khoản
        sendAccountStatusChangeNotification(updatedUser);

        return userMapper.userToUserDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public Page<UserDto> searchUsersByRole(UserRole role, String fullName, String email, String phone, Boolean active, Pageable pageable) {
        log.info("[ADMIN] Tìm kiếm người dùng với vai trò {} - Họ tên: {}, Email: {}, SĐT: {}, Trạng thái: {}",
                role, fullName, email, phone, active);

        Specification<User> spec = Specification.allOf(
                UserSpecification.withFullName(fullName),
                UserSpecification.withEmail(email),
                UserSpecification.withPhone(phone),
                UserSpecification.withRole(role),
                UserSpecification.isActive(active)
        );

        Page<User> usersPage = userRepository.findAll(spec, pageable);
        return usersPage.map(userMapper::userToUserDto);
    }

    /**
     * Gửi thông báo cho người dùng khi tài khoản bị thay đổi trạng thái (kích hoạt/vô hiệu hóa)
     */
    private void sendAccountStatusChangeNotification(User user) {
        try {
            if (user == null || user.getEmail() == null) {
                log.warn("[ADMIN] Không thể gửi thông báo thay đổi trạng thái tài khoản. User hoặc email null với user ID: {}", user != null ? user.getUserId() : "null");
                return;
            }

            User admin = authorizationService.getCurrentUserAndValidate();
            String status = user.isActive() ? "kích hoạt" : "vô hiệu hóa";
            String content = String.format("Tài khoản của bạn đã được quản trị viên %s.", status);
            String link = "/profile"; // Link tới trang cá nhân

            notificationService.createAndSendNotification(user.getEmail(), content, link, admin.getEmail());
            log.info("[ADMIN] Đã gửi thông báo thay đổi trạng thái tài khoản tới user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("[ADMIN] Lỗi khi gửi thông báo thay đổi trạng thái tài khoản cho user ID {}: {}", user.getUserId(), e.getMessage(), e);
        }
    }

    /**
     * Sinh mật khẩu ngẫu nhiên cho nhân viên mới
     * Sử dụng SecureRandom để đảm bảo tính bảo mật.
     * Mật khẩu trả về là chuỗi base64, giới hạn tối đa 8 ký tự.
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8]; // 8 bytes ~ 8 ký tự base64 (không padding)
        random.nextBytes(bytes);
        // Có thể bổ sung thêm ký tự đặc biệt nếu muốn tăng độ mạnh mật khẩu
        String pass = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        log.info("[ADMIN] Mật khẩu ngẫu nhiên đã được tạo.");
        return pass.substring(0, Math.min(pass.length(), 8)); // Giới hạn tối đa 8 ký tự
    }
}
