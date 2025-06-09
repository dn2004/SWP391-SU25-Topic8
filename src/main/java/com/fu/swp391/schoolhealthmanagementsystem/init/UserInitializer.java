package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.config.AdminProperties;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor; // Import cho constructor injection
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments; // Import mới
import org.springframework.boot.ApplicationRunner;   // Import mới
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserInitializer implements ApplicationRunner { // Thay đổi ở đây

    UserRepository userRepository; // Dùng final và constructor injection
    PasswordEncoder passwordEncoder;   // Dùng final và constructor injection
    AdminProperties adminProperties;


    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception { // Thay đổi ở đây
        log.info("Bắt đầu khởi tạo dữ liệu người dùng (sử dụng ApplicationRunner)...");

        // Bạn có thể truy cập các tham số dòng lệnh thông qua đối tượng 'args' nếu cần
        // Ví dụ: log.info("Source Args: {}", String.join(",", args.getSourceArgs()));
        // args.getOptionNames().forEach(optionName -> log.info("Option: {}, Values: {}", optionName, args.getOptionValues(optionName)));

        if (userRepository.existsByEmail(adminProperties.email())) {
            log.info("Tài khoản Admin với email {} đã tồn tại. Bỏ qua việc tạo.", adminProperties.email());
        } else {
            // Sử dụng @Builder từ Entity User (nếu có) hoặc setter như cũ
            User adminUser = User.builder() // Giả sử Entity User của bạn có @Builder
                    .email(adminProperties.email())
                    .password(passwordEncoder.encode(adminProperties.password()))
                    .fullName(adminProperties.fullName())
                    .role(UserRole.SchoolAdmin) // Đảm bảo UserRole có SchoolAdmin
                    .isActive(true)
                    .isFullNameConfirmed(true) // Giả sử bạn có trường này và muốn set mặc định
                    .build();

            // Nếu không dùng @Builder:
            // User adminUser = new User();
            // adminUser.setEmail(adminEmail);
            // adminUser.setPassword(passwordEncoder.encode(adminPassword));
            // adminUser.setFullName(adminFullName);
            // adminUser.setRole(UserRole.SchoolAdmin);
            // adminUser.setActive(true);
            // adminUser.setFullNameConfirmed(true);

            userRepository.save(adminUser);
            log.info("Đã tạo tài khoản Admin {} ({}) với vai trò {}.",
                    adminUser.getUsername(), adminUser.getEmail(), adminUser.getRole());
        }

        // Thêm logic khởi tạo dữ liệu người dùng khác nếu cần

        log.info("Hoàn tất khởi tạo dữ liệu người dùng.");
    }
}