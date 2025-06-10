package com.fu.swp391.schoolhealthmanagementsystem.security;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import com.fu.swp391.schoolhealthmanagementsystem.service.EmailService; // Import EmailService
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OtpService {
    private static final String OTP_PREFIX = "otp:password_reset:";
    private static final int OTP_LENGTH = 6;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    @Value("${otp.expiry.minutes:10}")
    private long otpExpiryMinutes;

    public void generateAndSendOtp(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Yêu cầu OTP cho email không tồn tại: {}", email);
                    return new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với email: " + email);
                });

        String otpCode = generateRandomOtp(OTP_LENGTH);
        String redisKey = OTP_PREFIX + email;

        redisTemplate.opsForValue().set(redisKey, otpCode, otpExpiryMinutes, TimeUnit.MINUTES);
        log.info("OTP được tạo cho email {}: {}. Lưu vào Redis với key {} trong {} phút.", email, otpCode, redisKey, otpExpiryMinutes);

        // Gửi OTP qua email
        emailService.sendOtpEmail(email, otpCode);
        // Không trả về OTP trong response API nữa
    }

    public void resetPasswordWithOtp(String email, String otp, String newPassword) {
        String redisKey = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(redisKey);

        if (storedOtp == null) {
            log.warn("OTP không tìm thấy hoặc đã hết hạn cho email: {}", email);
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP không hợp lệ hoặc đã hết hạn.");
        }

        if (!storedOtp.equals(otp)) {
            log.warn("OTP không hợp lệ được cung cấp cho email: {}. Mong đợi: {}, Thực tế: {}", email, storedOtp, otp);
            throw new AppException(HttpStatus.BAD_REQUEST, "OTP không hợp lệ.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy người dùng với email {} trong quá trình đặt lại mật khẩu, mặc dù OTP hợp lệ.", email);
                    return new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Không tìm thấy người dùng một cách không mong muốn.");
                });

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisTemplate.delete(redisKey);
        log.info("Đặt lại mật khẩu thành công cho email: {}. Đã xóa key OTP {}.", email, redisKey);
    }

    private String generateRandomOtp(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}