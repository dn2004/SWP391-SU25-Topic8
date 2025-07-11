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

        // 1. Khởi tạo tài khoản Phụ huynh
        User parent1;
        String parent1Email = "parent@example.com";
        if (userRepository.findByEmail(parent1Email).isEmpty()) {
            parent1 = User.builder()
                    .email(parent1Email)
                    .password(passwordEncoder.encode("password123"))
                    .fullName("John Parent")
                    .phoneNumber("0123456789")
                    .role(UserRole.Parent)
                    .active(true)
                    .build();
            parent1 = userRepository.save(parent1);
            log.info("Đã tạo tài khoản Phụ huynh: {}", parent1.getEmail());
        } else {
            parent1 = userRepository.findByEmail(parent1Email).get();
            log.info("Tài khoản Phụ huynh {} đã tồn tại.", parent1Email);
        }

        // 2. Khởi tạo Học sinh
        String studentFullName = "Alice Student";
        LocalDate studentDob = LocalDate.of(2015, 1, 1);
        Student student1;
        if (studentRepository.findByFullNameAndDateOfBirth(studentFullName, studentDob).isEmpty()) {
            student1 = new Student();
            student1.setFullName(studentFullName);
            student1.setDateOfBirth(studentDob);
            student1.setGender(Gender.FEMALE);
            student1.setClassGroup(ClassGroup.LA); // Sử dụng enum ClassGroup
            student1.setClassValue(Class.A); // Sử dụng enum Class
            student1.setInvitationCode(generateInvitationCode());
            student1.setStatus(StudentStatus.ACTIVE);
            student1 = studentRepository.save(student1);
            log.info("Đã tạo Học sinh: {}", student1.getFullName());
        } else {
            student1 = studentRepository.findByFullNameAndDateOfBirth(studentFullName, studentDob).get();
            log.info("Học sinh {} đã tồn tại.", studentFullName);
        }

        // 2.1. Khởi tạo Học sinh thứ 2
                String student2FullName = "Bob Student";
                LocalDate student2Dob = LocalDate.of(2016, 2, 2);
                Student student2;
                if (studentRepository.findByFullNameAndDateOfBirth(student2FullName, student2Dob).isEmpty()) {
                    student2 = new Student();
                    student2.setFullName(student2FullName);
                    student2.setDateOfBirth(student2Dob);
                    student2.setGender(Gender.MALE);
                    student2.setClassGroup(ClassGroup.MAM); // Sử dụng enum ClassGroup
                    student2.setClassValue(Class.B); // Sử dụng enum Class
                    student2.setInvitationCode(generateInvitationCode());
                    student2.setStatus(StudentStatus.ACTIVE);
                    student2 = studentRepository.save(student2);
                    log.info("Đã tạo Học sinh: {}", student2.getFullName());
                } else {
                    student2 = studentRepository.findByFullNameAndDateOfBirth(student2FullName, student2Dob).get();
                    log.info("Học sinh {} đã tồn tại.", student2FullName);
                }

                // 3.1. Liên kết Phụ huynh và Học sinh thứ 2
                if (!parentStudentLinkRepository.existsByParentAndStudent(parent1, student2)) {
                    ParentStudentLink link2 = new ParentStudentLink();
                    link2.setParent(parent1);
                    link2.setStudent(student2);
                    link2.setRelationshipType(RelationshipType.FATHER);
                    link2.setStatus(LinkStatus.ACTIVE);
                    parentStudentLinkRepository.save(link2);
                    log.info("Đã liên kết Phụ huynh {} với Học sinh {}.", parent1.getEmail(), student2.getFullName());
                } else {
                    log.info("Liên kết giữa Phụ huynh {} và Học sinh {} đã tồn tại.", parent1.getEmail(), student2.getFullName());
                }

        // 3. Liên kết Phụ huynh và Học sinh
        if (!parentStudentLinkRepository.existsByParentAndStudent(parent1, student1)) {
            ParentStudentLink link = new ParentStudentLink();
            link.setParent(parent1);
            link.setStudent(student1);

            link.setRelationshipType(RelationshipType.FATHER);
            link.setStatus(LinkStatus.ACTIVE);
            parentStudentLinkRepository.save(link);
            log.info("Đã liên kết Phụ huynh {} với Học sinh {}.", parent1.getEmail(), student1.getFullName());
        } else {
            log.info("Liên kết giữa Phụ huynh {} và Học sinh {} đã tồn tại.", parent1.getEmail(), student1.getFullName());
        }

        // 4. Khởi tạo tài khoản School Nurse (Y tá học đường)
        String schoolNurseEmail = "nurse@example.com";
        if (userRepository.findByEmail(schoolNurseEmail).isEmpty()) {
            User schoolNurse = User.builder()
                    .email(schoolNurseEmail)
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Mary Nurse")
                    .phoneNumber("0900112233")
                    .role(UserRole.MedicalStaff)
                    .active(true)
                    .build();
            userRepository.save(schoolNurse);
            log.info("Đã tạo tài khoản School Nurse: {}", schoolNurse.getEmail());
        } else {
            log.info("Tài khoản School Nurse {} đã tồn tại.", schoolNurseEmail);
        }

        // 5. Khởi tạo tài khoản Nurse Manager (Quản lý Y tá)
        String nurseManagerEmail = "nursemanager@example.com";
        if (userRepository.findByEmail(nurseManagerEmail).isEmpty()) {
            User nurseManager = User.builder()
                    .email(nurseManagerEmail)
                    .password(passwordEncoder.encode("password123"))
                    .fullName("Susan Manager")
                    .phoneNumber("0900445566")
                    .role(UserRole.StaffManager)
                    .active(true)
                    .build();
            userRepository.save(nurseManager);
            log.info("Đã tạo tài khoản Nurse Manager: {}", nurseManager.getEmail());
        } else {
            log.info("Tài khoản Nurse Manager {} đã tồn tại.", nurseManagerEmail);
        }
                log.info("Hoàn tất khởi tạo dữ liệu.");
    }

    private String generateInvitationCode() {
        return RandomStringUtils.random(10, true, true).toUpperCase();
    }
}