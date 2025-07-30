package com.fu.swp391.schoolhealthmanagementsystem.config;

import com.fu.swp391.schoolhealthmanagementsystem.config.converter.StringToMultipartFileConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringToMultipartFileConverter stringToMultipartFileConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToMultipartFileConverter);
    }
}
