package com.fu.swp391.schoolhealthmanagementsystem.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Health Management System API")
                        .version("v1")
                        .description("This is the API documentation for the School Health Management System. Project for the course SWP391 - Software Engineering Project at FPT University.")
                        .contact(new Contact()
                                .name("Nguyen Huu Dat")
                                .email("datnhqe180140@fpt.edu.vn")
                                .url("https://www.github.com/dn2004")
                        ))
                // Add security schemes
                .addSecurityItem(new SecurityRequirement().addList("parent-api"))
                .addSecurityItem(new SecurityRequirement().addList("admin-api"))
                .components(
                        new Components()
                                // JWT Bearer Authentication
                                .addSecuritySchemes("bearerAuth",
                                        new SecurityScheme()
                                                .name("bearerAuth")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                                // Parent API Authentication
                                .addSecuritySchemes("parent-api",
                                        new SecurityScheme()
                                                .name("parent-api")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .in(SecurityScheme.In.HEADER)
                                )
                                // Admin API Authentication
                                .addSecuritySchemes("admin-api",
                                        new SecurityScheme()
                                                .name("admin-api")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .in(SecurityScheme.In.HEADER)
                                )
                );
    }
}