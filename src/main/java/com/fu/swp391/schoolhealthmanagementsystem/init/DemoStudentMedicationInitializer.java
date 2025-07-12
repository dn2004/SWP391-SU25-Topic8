package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import com.fu.swp391.schoolhealthmanagementsystem.entity.ParentStudentLink;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentMedicationRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class DemoStudentMedicationInitializer implements ApplicationRunner {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMedicationRepository studentMedicationRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu StudentMedication mẫu...");

        try {
            List<Student> students = studentRepository.findAll();
            if (students.isEmpty()) {
                log.warn("Không tìm thấy học sinh nào. Hãy chạy DemoUserInitializer trước.");
                return;
            }

            User nurse = userRepository.findByEmail("nurse@example.com")
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng nurse@example.com."));

            int studentsToMedicate = 15;
            int medicationsCreatedCount = 0;

            for (int i = 0; i < students.size() && i < studentsToMedicate; i++) {
                Student student = students.get(i);

                if (studentMedicationRepository.countByStudent(student) > 0) {
                    log.info("Dữ liệu StudentMedication cho học sinh {} đã tồn tại. Bỏ qua khởi tạo.", student.getFullName());
                    continue;
                }

                Optional<ParentStudentLink> parentLinkOpt = parentStudentLinkRepository.findByStudent(student)
                        .stream()
                        .map(obj -> (ParentStudentLink) obj)
                        .findFirst();
                if (parentLinkOpt.isEmpty()) {
                    log.warn("Không tìm thấy phụ huynh cho học sinh {}. Bỏ qua tạo thuốc.", student.getFullName());
                    continue;
                }
                User parent = parentLinkOpt.get().getParent();

                LocalDate today = LocalDate.now();

                // Create 2-3 medications for each student
                createMedication(student, parent, nurse, "Panadol 500mg", "Uống 1 viên khi sốt trên 38.5 độ C", "1 viên", 20, 10, today.minusDays(10), today.plusDays(20), "Uống sau bữa ăn sáng", List.of(MedicationTimeSlot.builder().timeExpression("08:00").build()));
                medicationsCreatedCount++;

                createMedication(student, parent, nurse, "Ibuprofen 200mg", "Uống khi có chỉ định", "1 viên", 15, 5, today.minusDays(5), today.plusDays(15), "Uống khi đau", List.of(MedicationTimeSlot.builder().timeExpression("12:30").build()));
                medicationsCreatedCount++;

                // Add a third medication for some students
                if (i % 2 == 0) {
                    createMedication(student, parent, nurse, "Siro ho Prospan", "Uống 5ml/lần, 2 lần/ngày", "5ml", 100, 50, today.minusDays(2), today.plusMonths(1), "Lắc kỹ trước khi dùng", List.of(MedicationTimeSlot.builder().timeExpression("09:00").build(), MedicationTimeSlot.builder().timeExpression("16:00").build()));
                    medicationsCreatedCount++;
                }
            }

            log.info("Hoàn tất khởi tạo {} StudentMedication mẫu cho {} học sinh.", medicationsCreatedCount, studentsToMedicate);

        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu StudentMedication: {}", e.getMessage(), e);
        }
    }

    private void createMedication(Student student, User parent, User nurse,
                                  String name, String usageInstruction, String dosagePerAdministrationText,
                                  int totalDoses, int remainingDoses,
                                  LocalDate startDate, LocalDate expiryDate, String notes,
                                  List<MedicationTimeSlot> timeSlots) {
        LocalDate today = LocalDate.now();
        StudentMedication medication = StudentMedication.builder()
                .student(student)
                .submittedByParent(parent)
                .createdByUser(nurse)
                .receivedByMedicalStaff(nurse)
                .medicationName(name)
                .usageInstruction(usageInstruction)
                .dosagePerAdministrationText(dosagePerAdministrationText)
                .totalDosesProvided(totalDoses)
                .remainingDoses(remainingDoses)
                .scheduleStartDate(startDate)
                .expiryDate(expiryDate)
                .dateReceived(startDate)
                .notes(notes)
                .status(today.isAfter(expiryDate) ? MedicationStatus.EXPIRED : MedicationStatus.AVAILABLE)
                .build();

        timeSlots.forEach(medication::addMedicationTimeSlot);

        studentMedicationRepository.save(medication);
        log.info("Đã tạo StudentMedication: {} cho học sinh {}", medication.getMedicationName(), student.getFullName());
    }
}