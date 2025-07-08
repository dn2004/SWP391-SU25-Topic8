package com.fu.swp391.schoolhealthmanagementsystem.config;

import com.fu.swp391.schoolhealthmanagementsystem.prop.AdminProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.CloudinaryProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.FileStorageProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.JwtProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.LogoProperties;
import com.fu.swp391.schoolhealthmanagementsystem.prop.FrontEndProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AdminProperties.class,
        FileStorageProperties.class,
        CloudinaryProperties.class,
        JwtProperties.class,
        LogoProperties.class,
        FrontEndProperties.class
})
public class AppPropertiesConfiguration {

}