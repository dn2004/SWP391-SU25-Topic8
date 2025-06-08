package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.user.ChangePasswordRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.UserMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Không thể tìm thấy người dùng đã xác thực với email: {}", email);
                    // UsernameNotFoundException thường được xử lý bởi AuthenticationEntryPoint của Spring Security
                    // nếu nó xảy ra trong quá trình loadUserByUsername.
                    // Ở đây, nếu user đã auth mà không tìm thấy -> lỗi nghiêm trọng hơn.
                    return new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy thông tin người dùng đã xác thực.");
                });
    }

    public UserDto getCurrentUserDto() {
        User user = getCurrentAuthenticatedUser();
        return userMapper.userToUserDto(user);
    }


    @Transactional
    public void changePassword(ChangePasswordRequestDto requestDto) {
        User user = getCurrentAuthenticatedUser();
        log.info("Người dùng {} đang thay đổi mật khẩu.", user.getEmail());

        if (!StringUtils.hasText(user.getPassword())) {
            log.warn("Người dùng {} chưa đặt mật khẩu. Không thể sử dụng API 'change-password'. Hãy dùng 'set-password'.", user.getEmail());
            throw new AppException(HttpStatus.BAD_REQUEST, "Bạn chưa đặt mật khẩu. Vui lòng sử dụng chức năng đặt mật khẩu mới.");
        }

        if (!passwordEncoder.matches(requestDto.oldPassword(), user.getPassword())) {
            log.warn("Người dùng {} nhập sai mật khẩu cũ.", user.getEmail());
            throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không chính xác.");
        }
        if (passwordEncoder.matches(requestDto.newPassword(), user.getPassword())) {
            log.warn("Người dùng {} cố gắng đặt mật khẩu mới trùng mật khẩu cũ.", user.getEmail());
            throw new AppException(HttpStatus.BAD_REQUEST, "Mật khẩu mới không được trùng với mật khẩu cũ.");
        }

        user.setPassword(passwordEncoder.encode(requestDto.newPassword()));
        userRepository.save(user);
        log.info("Đã thay đổi mật khẩu thành công cho {}.", user.getEmail());
    }

    // Add other user profile update methods here
}