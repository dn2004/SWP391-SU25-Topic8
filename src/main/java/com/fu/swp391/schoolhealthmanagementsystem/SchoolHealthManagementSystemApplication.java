package com.fu.swp391.schoolhealthmanagementsystem;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@OpenAPIDefinition(
        info = @Info(
                title = "School Health Management System API",
                description = "This is the API documentation for the School Health Management System. Project for the course SWP391 - Software Engineering Project at FPT University.",
                version = "v1",
                contact = @Contact(
                        name = "Nguyen Huu Dat",
                        email = "datnhqe180140@fpt.edu.vn",
                        url = "https://www.github.com/dn2004"
                )
        )
)
@SecuritySchemes({
        @SecurityScheme(
                name = "parent-api",
                scheme = "bearer",
                type = SecuritySchemeType.HTTP,
                in = SecuritySchemeIn.HEADER
        ),
        @SecurityScheme(
                name = "admin-api",
                scheme = "bearer",
                type = SecuritySchemeType.HTTP,
                in = SecuritySchemeIn.HEADER
        )
})
public class SchoolHealthManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolHealthManagementSystemApplication.class, args);
    }

}
