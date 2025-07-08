package com.fu.swp391.schoolhealthmanagementsystem.config;

import com.cloudinary.Cloudinary;
import com.fu.swp391.schoolhealthmanagementsystem.prop.CloudinaryProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private final CloudinaryProperties cloudinaryProperties;

    @Autowired
    public CloudinaryConfig(CloudinaryProperties cloudinaryProperties) {
        this.cloudinaryProperties = cloudinaryProperties;
    }

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinaryProperties.cloudName());
        config.put("api_key", cloudinaryProperties.apiKey());
        config.put("api_secret", cloudinaryProperties.apiSecret());
        config.put("secure", "true");
        return new Cloudinary(config);
    }
}