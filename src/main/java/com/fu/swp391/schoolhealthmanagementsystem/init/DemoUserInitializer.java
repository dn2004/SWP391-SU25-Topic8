package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DemoUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu...");

        // 1. Khởi tạo 30 tài khoản Phụ huynh
        List<User> parents = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            String parentEmail = "parent" + i + "@example.com";
            User parent;
            if (userRepository.findByEmail(parentEmail).isEmpty()) {
                parent = User.builder()
                        .email(parentEmail)
                        .password(passwordEncoder.encode("password123"))
                        .fullName("Phụ huynh " + i)
                        .phoneNumber("01234567" + String.format("%02d", i))
                        .role(UserRole.Parent)
                        .active(true)
                        .build();
                parent = userRepository.save(parent);
                log.info("Đã tạo tài khoản Phụ huynh: {}", parent.getEmail());
            } else {
                parent = userRepository.findByEmail(parentEmail).get();
                log.info("Tài khoản Phụ huynh {} đã tồn tại.", parentEmail);
            }
            parents.add(parent);
        }

        // 2. Khởi tạo 50 Học sinh
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            String studentFullName = "Học sinh " + i;
            LocalDate studentDob = LocalDate.of(2015, (i % 12) + 1, (i % 28) + 1);
            Student student;
            if (studentRepository.findByFullNameAndDateOfBirth(studentFullName, studentDob).isEmpty()) {
                student = new Student();
                student.setFullName(studentFullName);
                student.setDateOfBirth(studentDob);
                student.setGender(i % 2 == 0 ? Gender.FEMALE : Gender.MALE);
                student.setClassGroup(ClassGroup.values()[i % ClassGroup.values().length]);
                student.setClassValue(Class.values()[i % Class.values().length]);
                student.setInvitationCode(generateInvitationCode());
                student.setStatus(StudentStatus.ACTIVE);
                student = studentRepository.save(student);
                log.info("Đã tạo Học sinh: {}", student.getFullName());
            } else {
                student = studentRepository.findByFullNameAndDateOfBirth(studentFullName, studentDob).get();
                log.info("Học sinh {} đã tồn tại.", studentFullName);
            }
            students.add(student);
        }

        // 3. Liên kết Phụ huynh và Học sinh
        if (!parents.isEmpty() && !students.isEmpty()) {
            Random random = new Random();
            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);

                // Gán phụ huynh thứ nhất (luôn có)
                User parent1 = parents.get(i % parents.size());
                if (!parentStudentLinkRepository.existsByParentAndStudent(parent1, student)) {
                    ParentStudentLink link1 = new ParentStudentLink();
                    link1.setParent(parent1);
                    link1.setStudent(student);
                    link1.setRelationshipType(RelationshipType.FATHER); // Mặc định là FATHER
                    link1.setStatus(LinkStatus.ACTIVE);
                    parentStudentLinkRepository.save(link1);
                    log.info("Đã liên kết Phụ huynh 1 (FATHER) {} với Học sinh {}.", parent1.getEmail(), student.getFullName());
                } else {
                    log.info("Liên kết giữa Phụ huynh 1 ({}) và Học sinh {} đã tồn tại.", parent1.getEmail(), student.getFullName());
                }

                // 50% cơ hội gán thêm phụ huynh thứ hai
                if (random.nextBoolean()) {
                    User parent2;
                    // Đảm bảo phụ huynh 2 khác phụ huynh 1
                    if (parents.size() > 1) { // Cần ít nhất 2 phụ huynh để thực hiện
                        do {
                            parent2 = parents.get(random.nextInt(parents.size()));
                        } while (Objects.equals(parent2.getUserId(), parent1.getUserId()));

                        if (!parentStudentLinkRepository.existsByParentAndStudent(parent2, student)) {
                            ParentStudentLink link2 = new ParentStudentLink();
                            link2.setParent(parent2);
                            link2.setStudent(student);
                            link2.setRelationshipType(RelationshipType.MOTHER); // Phụ huynh 2 là MOTHER
                            link2.setStatus(LinkStatus.ACTIVE);
                            parentStudentLinkRepository.save(link2);
                            log.info("Đã liên kết Phụ huynh 2 (MOTHER) {} với Học sinh {}.", parent2.getEmail(), student.getFullName());
                        }
                    }
                }
            }
        }

        // 4. Khởi tạo 10 tài khoản School Nurse (Y tá học đường)
        for (int i = 1; i <= 10; i++) {
            String schoolNurseEmail = "nurse" + i + "@example.com";
            if (userRepository.findByEmail(schoolNurseEmail).isEmpty()) {
                User schoolNurse = User.builder()
                        .email(schoolNurseEmail)
                        .password(passwordEncoder.encode("password123"))
                        .fullName("Mary Nurse " + i)
                        .phoneNumber("09001122" + String.format("%02d", i))
                        .role(UserRole.MedicalStaff)
                        .active(true)
                        .build();
                userRepository.save(schoolNurse);
                log.info("Đã tạo tài khoản School Nurse: {}", schoolNurse.getEmail());
            } else {
                log.info("Tài khoản School Nurse {} đã tồn tại.", schoolNurseEmail);
            }
        }

        // 5. Khởi tạo 10 tài khoản Nurse Manager (Quản lý Y tá)
        for (int i = 1; i <= 10; i++) {
            String nurseManagerEmail = "nursemanager" + i + "@example.com";
            if (userRepository.findByEmail(nurseManagerEmail).isEmpty()) {
                User nurseManager = User.builder()
                        .email(nurseManagerEmail)
                        .password(passwordEncoder.encode("password123"))
                        .fullName("Susan Manager " + i)
                        .phoneNumber("09004455" + String.format("%02d", i))
                        .role(UserRole.StaffManager)
                        .active(true)
                        .build();
                userRepository.save(nurseManager);
                log.info("Đã tạo tài khoản Nurse Manager: {}", nurseManager.getEmail());
            } else {
                log.info("Tài khoản Nurse Manager {} đã tồn tại.", nurseManagerEmail);
            }
        }
        log.info("Hoàn tất khởi tạo dữ liệu.");
    }

    private String generateInvitationCode() {
        return RandomStringUtils.random(10, true, true).toUpperCase();
    }
}
