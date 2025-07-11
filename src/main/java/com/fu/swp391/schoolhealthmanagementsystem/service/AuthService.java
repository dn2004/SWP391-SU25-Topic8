package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.auth.*;
import com.fu.swp391.schoolhealthmanagementsystem.dto.user.UserDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.UserMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import com.fu.swp391.schoolhealthmanagementsystem.security.JwtService;
import com.fu.swp391.schoolhealthmanagementsystem.security.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final OtpService otpService;
    private final NotificationService notificationService;

    @Value("${jwt.blacklist.prefix:jwt:blacklist:}")
    private String blacklistPrefix;

    @Transactional
    public UserDto registerParent(RegisterParentRequestDto dto) {
        log.info("Đăng ký tài khoản phụ huynh mới cho email: {}", dto.email());
        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Email {} đã tồn tại.", dto.email());
            throw new AppException(HttpStatus.BAD_REQUEST, "Email đã được sử dụng!");
        }

        User parent = User.builder()
                .fullName(dto.fullName())
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password())) // Chỉ mã hóa password chính
                .phoneNumber(dto.phoneNumber())
                .role(UserRole.Parent)
                // .active(true) // Đã có @Builder.Default
                .build();

        User savedParent = userRepository.save(parent);
        log.info("Đã tạo tài khoản phụ huynh thành công: {}", savedParent.getEmail());

        // Gửi thông báo cho admin
        sendNewParentRegistrationNotification(savedParent);

        return userMapper.userToUserDto(savedParent);
    }

    private void sendNewParentRegistrationNotification(User parent) {
        try {
            String content = String.format("Có tài khoản phụ huynh mới vừa đăng ký: %s (%s).", parent.getFullName(), parent.getEmail());
            String link = "/admin/users/parents"; // Link tới trang quản lý phụ huynh
            notificationService.createAndSendNotificationToRole(UserRole.SchoolAdmin, content, link, parent.getEmail());
            log.info("Đã gửi thông báo đăng ký phụ huynh mới tới SchoolAdmin cho tài khoản: {}", parent.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo đăng ký phụ huynh mới cho tài khoản {}: {}", parent.getEmail(), e.getMessage(), e);
        }
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        log.info("Xác thực người dùng: {}", dto.email());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

            if (!user.isActive()) {
                log.warn("Tài khoản {} chưa được kích hoạt hoặc đã bị vô hiệu hóa.", dto.email());
                // Ném AuthenticationException để GlobalExceptionHandler xử lý như một lỗi đăng nhập
                throw new DisabledException("Tài khoản chưa được kích hoạt hoặc đã bị vô hiệu hóa.");
            }

        String jwt = jwtService.generateToken(user);
        log.info("Đăng nhập thành công cho: {}", user.getEmail());
        UserDto userDto = userMapper.userToUserDto(user);

        return new LoginResponseDto(jwt, userDto);
        } catch (AuthenticationException e) {
            log.warn("Lỗi xác thực cho người dùng {}: {}", dto.email(), e.getMessage());
            throw e; // Để GlobalExceptionHandler xử lý
        }
    }


    public void logout(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("Không có token để logout.");
            return;
        }
        final String jwt = authHeader.substring(7);
        if (!StringUtils.hasText(jwt) || !jwtService.validateToken(jwt)) {
            log.warn("Token không hợp lệ hoặc đã hết hạn, không cần blacklist.");
            return;
        }

        try {
            var claims = jwtService.getClaimsFromToken(jwt);
            if (claims == null) {
                log.warn("Không thể lấy claims từ token hợp lệ, không thể blacklist JTI.");
                long expiresIn = jwtService.getClaimsToParseForBlacklist(jwt).getExpirationTime().getTime() - System.currentTimeMillis();
                if (expiresIn > 0) {
                    redisTemplate.opsForValue().set(blacklistPrefix + jwt, "logged_out", expiresIn, TimeUnit.MILLISECONDS);
                    log.info("Token (string) đã được blacklist. Key: {}{}", blacklistPrefix, jwt.substring(0,10)+"...");
                }
                return;
            }

            String jti = claims.getJWTID();
            Date expirationTime = claims.getExpirationTime();

            if (jti != null && expirationTime != null) {
                long expiresIn = expirationTime.getTime() - System.currentTimeMillis();
                if (expiresIn > 0) {
                    redisTemplate.opsForValue().set(blacklistPrefix + jti, "logged_out", expiresIn, TimeUnit.MILLISECONDS);
                    log.info("Token với JTI {} đã được blacklist.", jti);
                } else {
                    log.info("Token với JTI {} đã hết hạn, không cần blacklist.", jti);
                }
            } else {
                log.warn("Không tìm thấy JTI hoặc thời gian hết hạn trong token, không thể blacklist bằng JTI.");
                // Fallback: blacklist the token string itself
                long expiresIn = jwtService.getClaimsToParseForBlacklist(jwt).getExpirationTime().getTime() - System.currentTimeMillis();
                if (expiresIn > 0) {
                    redisTemplate.opsForValue().set(blacklistPrefix + jwt, "logged_out", expiresIn, TimeUnit.MILLISECONDS);
                    log.info("Token (string) đã được blacklist. Key: {}{}", blacklistPrefix, jwt.substring(0,10)+"...");
                }
            }
        } catch (Exception e) {
            log.error("Lỗi khi blacklist token: {}", e.getMessage(), e);
            // Fallback: attempt to blacklist the raw token string if claims parsing failed after validation
            try {
                var claimsForBlacklist = jwtService.getClaimsToParseForBlacklist(jwt);
                if (claimsForBlacklist != null && claimsForBlacklist.getExpirationTime() != null) {
                    long expiresIn = claimsForBlacklist.getExpirationTime().getTime() - System.currentTimeMillis();
                    if (expiresIn > 0) {
                        redisTemplate.opsForValue().set(blacklistPrefix + jwt, "logged_out_fallback", expiresIn, TimeUnit.MILLISECONDS);
                        log.info("Token (string fallback) đã được blacklist. Key: {}{}", blacklistPrefix, jwt.substring(0,10)+"...");
                    }
                }
            } catch (Exception ex) {
                log.error("Lỗi nghiêm trọng khi cố gắng blacklist token (fallback): {}", ex.getMessage(), ex);
            }
        }
    }

    public void forgotPassword(ForgotPasswordRequestDto dto) {
        log.info("Yêu cầu quên mật khẩu cho email: {}", dto.email());
        otpService.generateAndSendOtp(dto.email());
        log.info("Đã gửi yêu cầu OTP cho {}", dto.email());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto dto) {
        log.info("Yêu cầu đặt lại mật khẩu cho email: {}", dto.email());
        otpService.resetPasswordWithOtp(dto.email(), dto.otp(), dto.newPassword());
        log.info("Mật khẩu đã được đặt lại thành công cho {}", dto.email());
    }
}