package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.MedicationTimeSlot;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentMedication;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
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

@Component
@Slf4j
@RequiredArgsConstructor
@Order(3)
public class DemoStudentMedicationInitializer implements ApplicationRunner {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMedicationRepository studentMedicationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu StudentMedication mẫu...");

        try {
            // Tìm học sinh và người dùng được tạo trong DemoUserInitializer
            Student student = studentRepository.findByFullNameAndDateOfBirth("Alice Student", LocalDate.of(2015, 1, 1))
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy học sinh Alice Student. Hãy chạy DemoUserInitializer trước."));
            User parent = userRepository.findByEmail("parent@example.com")
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng parent@example.com."));
            User nurse = userRepository.findByEmail("nurse@example.com")
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng nurse@example.com."));

            // Kiểm tra xem có nên khởi tạo dữ liệu không
            if (studentMedicationRepository.countByStudent(student) > 0) {
                log.info("Dữ liệu StudentMedication cho học sinh {} đã tồn tại. Bỏ qua khởi tạo.", student.getFullName());
                return;
            }

            LocalDate today = LocalDate.now();

            // Thuốc 1: Hết hạn hôm qua
            createMedication(
                    student, parent, nurse,
                    "Panadol 500mg",
                    "Uống 1 viên khi sốt trên 38.5 độ C",
                    "1 viên",
                    20,
                    10,
                    today.minusDays(10),
                    today.minusDays(1), // Ngày hết hạn
                    "Uống sau bữa ăn sáng",
                    List.of(
                            MedicationTimeSlot.builder().timeExpression("08:00").build()
                    )
            );

            // Thuốc 2: Hết hạn 2 ngày trước
            createMedication(
                    student, parent, nurse,
                    "Ibuprofen 200mg",
                    "Uống khi có chỉ định",
                    "1 viên",
                    15,
                    5,
                    today.minusDays(20),
                    today.minusDays(2), // Ngày hết hạn
                    "Uống khi đau",
                    List.of(
                            MedicationTimeSlot.builder().timeExpression("12:30").build()
                    )
            );

            // Thuốc 3: Còn hạn
            createMedication(
                    student, parent, nurse,
                    "Amoxicillin 250mg",
                    "Uống 3 lần/ngày, mỗi lần 1 viên",
                    "1 viên",
                    21,
                    15,
                    today.minusDays(8),
                    today.plusWeeks(1), // Ngày hết hạn
                    "Uống đủ 7 ngày",
                    List.of(
                            MedicationTimeSlot.builder().timeExpression("07:00").build(),
                            MedicationTimeSlot.builder().timeExpression("13:00").build()
                    )
            );

            // Thuốc 4: Còn hạn.
            createMedication(
                    student, parent, nurse,
                    "Loratadine 10mg",
                    "Uống 1 viên mỗi ngày vào buổi sáng",
                    "1 viên",
                    30,
                    20,
                    today.minusDays(40),
                    today.plusDays(10),                     "Chống dị ứng",
                    List.of(
                            MedicationTimeSlot.builder().timeExpression("08:30").build()
                    )
            );

            // Thuốc 5:
            createMedication(
                    student, parent, nurse,
                    "Siro ho Prospan",
                    "Uống 5ml/lần, 2 lần/ngày",
                    "5ml",
                    100, // tổng ml
                    50,  // ml còn lại
                    today.minusDays(5),
                    today.plusMonths(1),
                    "Lắc kỹ trước khi dùng",
                    List.of(
                            MedicationTimeSlot.builder().timeExpression("09:00").build(),
                            MedicationTimeSlot.builder().timeExpression("16:00").build()
                    )
            );

            log.info("Hoàn tất khởi tạo 5 StudentMedication mẫu.");

        } catch (IllegalStateException e) {
            log.error("Lỗi khi khởi tạo dữ liệu StudentMedication: {}", e.getMessage());
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
