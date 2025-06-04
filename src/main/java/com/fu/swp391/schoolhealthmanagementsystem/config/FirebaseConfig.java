package com.fu.swp391.schoolhealthmanagementsystem.config; // Điều chỉnh package cho phù hợp

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @PostConstruct
    public void initialize() {
        try {
            String serviceAccountPath = "firebase/firebase-service-account.json"; // Đường dẫn đến file JSON của Firebase service account
            InputStream serviceAccount = new ClassPathResource(serviceAccountPath).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase application has been initialized successfully.");
            }
        } catch (IOException e) {
            logger.error("Error initializing Firebase Admin SDK: {}", e.getMessage(), e);
        }
    }
}