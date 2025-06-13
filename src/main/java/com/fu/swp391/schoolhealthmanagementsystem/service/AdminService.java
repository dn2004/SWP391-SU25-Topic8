package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.admin.CreateStaffRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.UserMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        log.info("Admin yêu cầu lấy thông tin người dùng với ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Admin: Không tìm thấy người dùng với ID: {}", userId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + userId);
                });
        return userMapper.userToUserDto(user);
    }

    @Transactional
    public UserDto createStaffAccount(CreateStaffRequestDto dto) {
        log.info("Admin tạo tài khoản nhân viên mới cho email: {}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Email {} đã tồn tại khi admin cố tạo tài khoản.", dto.email());
            throw new AppException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng!");
        }
        if (dto.role() != UserRole.MedicalStaff && dto.role() != UserRole.StaffManager) {
            log.warn("Admin cố gắng tạo tài khoản với vai trò không hợp lệ: {}", dto.role());
            throw new AppException(HttpStatus.BAD_REQUEST, "Vai trò nhân viên không hợp lệ.");
        }

        User staff = new User();
        staff.setFullName(dto.fullName());
        staff.setEmail(dto.email());
        staff.setPhoneNumber(dto.phoneNumber());
        staff.setRole(dto.role());
        staff.setActive(true); // Kích hoạt ngay

        String randomPassword = generateRandomPassword();
        staff.setPassword(passwordEncoder.encode(randomPassword));

        User savedStaff = userRepository.save(staff);
        log.info("Admin đã tạo tài khoản nhân viên {} thành công: {}", dto.role(), savedStaff.getEmail());

        // Gửi email thông tin đăng nhập
        emailService.sendNewStaffCredentialsEmail(savedStaff, randomPassword);

        return userMapper.userToUserDto(savedStaff);
    }

    @Transactional
    public UserDto updateUserActivationStatus(Long userId, boolean isActive) {
        log.info("Admin cập nhật trạng thái kích hoạt cho user ID {}: {}", userId, isActive ? "Kích hoạt" : "Vô hiệu hóa");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Admin: Không tìm thấy user ID {} để cập nhật trạng thái.", userId);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng.");
                });

        // Optional: Prevent deactivating self or other admins if needed
        // Authentication currentUserAuth = SecurityContextHolder.getContext().getAuthentication();
        // if (currentUserAuth.getPrincipal() instanceof User) {
        //     User adminUser = (User) currentUserAuth.getPrincipal();
        //     if (adminUser.getUserId().equals(userId)) {
        //         throw new AppException(HttpStatus.BAD_REQUEST, "Bạn không thể tự vô hiệu hóa tài khoản của mình.");
        //     }
        // }
        // if (user.getRole() == UserRole.SchoolAdmin && !active) {
        //      throw new AppException(HttpStatus.BAD_REQUEST, "Không thể vô hiệu hóa tài khoản SchoolAdmin khác.");
        // }


        user.setActive(isActive);
        User updatedUser = userRepository.save(user);
        log.info("Đã cập nhật trạng thái người dùng ID {} thành {}", userId, isActive ? "active" : "inactive");
        return userMapper.userToUserDto(updatedUser);
    }

    public Page<UserDto> getUsersByRole(UserRole role, Pageable pageable) {
        log.info("Admin lấy danh sách người dùng với vai trò: {} - Trang: {}, Kích thước: {}", role, pageable.getPageNumber(), pageable.getPageSize());
        Page<User> usersPage = userRepository.findAllByRole(role, pageable);
        return usersPage.map(userMapper::userToUserDto);
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12]; // 12 bytes = 16 Base64 characters
        random.nextBytes(bytes);
        // Make it more human-readable if needed, e.g., alphanumeric
        // For now, Base64 is fine for initial password
        String pass = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        // Ensure it meets complexity if you have strict rules, e.g., add a number and symbol
        // Example: pass + random.nextInt(10) + "!@#$%^&*".charAt(random.nextInt(8));
        // Keep it simple for this example, or use a library like Passay
        log.debug("Mật khẩu ngẫu nhiên đã được tạo.");
        return pass.substring(0, Math.min(pass.length(), 16)); // Ensure max length
    }
}