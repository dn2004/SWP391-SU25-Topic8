package com.fu.swp391.schoolhealthmanagementsystem.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

// Trong class Main Application hoặc một @Component khởi tạo sớm
@Component
public class EnvironmentChecker {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentChecker.class);

    @Value("${spring.profiles.active:UNKNOWN_PROFILE}")
    private String activeProfile;

    @Value("${server.port:UNKNOWN_PORT}")
    private String serverPort;

    @Value("${spring.datasource.username:UNKNOWN_DB_USER}")
    private String dbUser;

    @Value("${jwt.secret:UNKNOWN_JWT_SECRET}")
    private String jwtSecret;

    // Hoặc đọc trực tiếp từ Environment
    @Autowired
    private Environment environment;

    @PostConstruct
    public void checkEnv() {
        logger.info("---- CHECKING ENVIRONMENT ----");
        logger.info("Spring Profile (from @Value): {}", activeProfile);
        logger.info("Server Port (from @Value): {}", serverPort);
        logger.info("DB User (from @Value): {}", dbUser);
        logger.info("JWT Secret (from @Value): '{}'", jwtSecret); // Log giá trị thật cẩn thận nếu là dev

        logger.info("DB_USER (from System.getenv): {}", System.getenv("DB_USER"));
        logger.info("PORT (from System.getenv): {}", System.getenv("PORT"));
        logger.info("PROFILE (from System.getenv): {}", System.getenv("PROFILE"));
        logger.info("JWT_SECRET (from System.getenv): '{}'", System.getenv("JWT_SECRET"));

        // Cách khác để đọc từ Spring Environment (bao gồm cả properties và env vars)
        logger.info("spring.profiles.active (from Spring Env): {}", environment.getProperty("spring.profiles.active"));
        logger.info("server.port (from Spring Env): {}", environment.getProperty("server.port"));
        logger.info("spring.datasource.username (from Spring Env): {}", environment.getProperty("spring.datasource.username"));
        logger.info("jwt.secret (from Spring Env): '{}'", environment.getProperty("jwt.secret"));
        logger.info("-----------------------------");
    }
}