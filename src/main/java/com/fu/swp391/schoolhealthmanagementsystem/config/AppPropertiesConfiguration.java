package com.fu.swp391.schoolhealthmanagementsystem.config;

import com.fu.swp391.schoolhealthmanagementsystem.prop.AdminProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.CloudinaryProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.FileStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AdminProperties.class,
        FileStorageProperties.class,
        CloudinaryProperties.class
}) // Kích hoạt binding cho AdminProperties
public class AppPropertiesConfiguration {

}