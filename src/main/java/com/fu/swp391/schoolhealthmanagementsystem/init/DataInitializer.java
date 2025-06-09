package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Create parent user
        User parent = User.builder()
                .email("parent@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("John Parent")
                .phoneNumber("0123456789")
                .role(UserRole.Parent)
                .isActive(true)
                .build();
        userRepository.save(parent);

        // Create student
        Student student = new Student();
        student.setStudentCode("ST001");
        student.setFullName("Alice Student");
        student.setDateOfBirth(LocalDate.of(2015, 1, 1));
        student.setGender(Gender.FEMALE);
        student.setClassName("Class 3A");
        student.setAddress("123 Student Street");
        student.setInvitationCode(generateInvitationCode());
        student.setActive(true);
        studentRepository.save(student);

        // Create parent-student link
        ParentStudentLink link = new ParentStudentLink();
        link.setParent(parent);
        link.setStudent(student);
        link.setRelationshipType(RelationshipType.FATHER);
        link.setStatus(LinkStatus.ACTIVE);
        parentStudentLinkRepository.save(link);
    }

    private String generateInvitationCode() {
        return RandomStringUtils.random(10, true, true).toUpperCase();
    }
}
