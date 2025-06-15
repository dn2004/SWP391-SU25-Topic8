package com.fu.swp391.schoolhealthmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SchoolHealthManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolHealthManagementSystemApplication.class, args);
    }

}
