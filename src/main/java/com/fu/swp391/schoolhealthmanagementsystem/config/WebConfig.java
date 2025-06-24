package com.fu.swp391.schoolhealthmanagementsystem.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Các converter sẽ tự động được Spring đăng ký do đã được đánh dấu @Component
        log.info("Đã cấu hình WebConfig với các Enum converters");
    }
}
