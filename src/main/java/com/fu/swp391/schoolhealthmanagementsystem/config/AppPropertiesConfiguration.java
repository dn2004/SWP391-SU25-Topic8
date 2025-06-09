package com.fu.swp391.schoolhealthmanagementsystem.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AdminProperties.class,
        FileStorageProperties.class
}) // Kích hoạt binding cho AdminProperties
public class AppPropertiesConfiguration {

}