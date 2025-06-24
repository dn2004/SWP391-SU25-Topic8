package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.prop.AdminProperties;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor; // Import cho constructor injection
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments; // Import mới
import org.springframework.boot.ApplicationRunner;   // Import mới
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Order(1)
public class UserInitializer implements ApplicationRunner { // Thay đổi ở đây

    UserRepository userRepository; // Dùng final và constructor injection
    PasswordEncoder passwordEncoder;   // Dùng final và constructor injection
    AdminProperties adminProperties;

    private static final String SYSTEM_EMAIL = "system@example.com";
    private static final String SYSTEM_PASSWORD = "StrongP@ssw0rd!123";
    private static final String SYSTEM_NAME = "System User";

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Bắt đầu khởi tạo dữ liệu người dùng (sử dụng ApplicationRunner)...");

        // Khởi tạo tài khoản admin
        if (userRepository.existsByEmail(adminProperties.email())) {
            log.info("Tài khoản Admin với email {} đã tồn tại. Bỏ qua việc tạo.", adminProperties.email());
        } else {
            // Sử dụng @Builder từ Entity User (nếu có) hoặc setter như cũ
            User adminUser = User.builder() // Giả sử Entity User của bạn có @Builder
                    .email(adminProperties.email())
                    .password(passwordEncoder.encode(adminProperties.password()))
                    .fullName(adminProperties.fullName())
                    .role(UserRole.SchoolAdmin) // Đảm bảo UserRole có SchoolAdmin
                    .active(true)
                    .build();

            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản Admin {} ({}) với vai trò {}.",
                    adminUser.getUsername(), adminUser.getEmail(), adminUser.getRole());
        }

        // Khởi tạo tài khoản system cho các scheduler tự động
        if (userRepository.existsByEmail(SYSTEM_EMAIL)) {
            log.info("Tài khoản System với email {} đã tồn tại. Bỏ qua việc tạo.", SYSTEM_EMAIL);
        } else {
            User systemUser = User.builder()
                    .email(SYSTEM_EMAIL)
                    .password(passwordEncoder.encode(SYSTEM_PASSWORD))
                    .fullName(SYSTEM_NAME)
                    .role(UserRole.SchoolAdmin) // Gán quyền admin cho tài khoản hệ thống
                    .active(true)
                    .build();

            userRepository.save(systemUser);
            log.info("Đã tạo tài khoản System {} ({}) cho các tiến trình tự động.",
                    systemUser.getUsername(), systemUser.getEmail());
        }
        log.info("Hoàn tất khởi tạo dữ liệu người dùng.");
    }
}