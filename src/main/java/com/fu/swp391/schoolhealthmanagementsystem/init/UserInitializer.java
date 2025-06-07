package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class UserInitializer implements CommandLineRunner {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // You can externalize these admin details to application.properties
    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.fullname:System Administrator}")
    private String adminFullName;


    @Override
    @Transactional // Good practice to wrap db operations in a transaction
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // Check if the admin user already exists by email or username
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user with email {} or already exists. Skipping creation.", adminEmail);
        } else {
            User adminUser = new User();
            adminUser.setEmail(adminEmail);
            adminUser.setPassword(passwordEncoder.encode(adminPassword)); // Always encode passwords
            adminUser.setFullName(adminFullName);
            adminUser.setRole(UserRole.SchoolAdmin);
            adminUser.setActive(true);
            // CreatedAt and UpdatedAt will be set by @CreationTimestamp and @UpdateTimestamp

            userRepository.save(adminUser);
            log.info("Admin user {} ({}) created successfully with role {}.",
                    adminUser.getUsername(), adminUser.getEmail(), adminUser.getRole());
        }

        // You can add more initial data here if needed (e.g., default medical staff)

        log.info("Data initialization finished.");
    }
}