package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import com.fu.swp391.schoolhealthmanagementsystem.service.MedicalSupplyService; // Import service
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(4)
public class DemoHealthIncidentInitializer implements ApplicationRunner {

    private final HealthIncidentRepository healthIncidentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final MedicalSupplyService medicalSupplyService;
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu Sự cố Sức khỏe...");

        // Lấy Học sinh và Y tá đã được tạo
        Optional<Student> studentOpt = studentRepository.findByFullNameAndDateOfBirth("Alice Student", LocalDate.of(2015, 1, 1));
        Optional<User> nurseOpt = userRepository.findByEmail("nurse@example.com");
        Optional<MedicalSupply> paracetamolOpt = medicalSupplyRepository.findFirstByName("Thuốc hạ sốt Paracetamol 500mg");
        Optional<MedicalSupply> bandageOpt = medicalSupplyRepository.findFirstByName("Băng gạc y tế tiệt trùng");

        if (studentOpt.isEmpty() || nurseOpt.isEmpty() || paracetamolOpt.isEmpty() || bandageOpt.isEmpty()) {
            log.warn("Không tìm thấy học sinh, y tá hoặc vật tư y tế cần thiết. Bỏ qua khởi tạo sự cố sức khỏe.");
            return;
        }

        Student student = studentOpt.get();
        User nurse = nurseOpt.get();

        // Sự cố 1: Sốt nhẹ
        // Kiểm tra xem sự cố tương tự đã tồn tại chưa (ví dụ bằng cách kiểm tra description hoặc một trường unique khác nếu có)
        // Hiện tại, chúng ta sẽ tạo mới nếu chưa có bất kỳ sự cố nào cho học sinh này
        if (healthIncidentRepository.findByStudent(student, org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
            HealthIncident incident1 = HealthIncident.builder()
                    .student(student)
                    .recordedByUser(nurse)
                    .incidentDateTime(LocalDateTime.now().minusHours(2)) // 2 giờ trước
                    .incidentType(HealthIncidentType.ILLNESS)
                    .description("Học sinh cảm thấy mệt mỏi, nhiệt độ 37.8°C.")
                    .actionTaken("Cho học sinh nghỉ ngơi tại phòng y tế, theo dõi nhiệt độ. Cho uống 1 viên Paracetamol.")
                    .location("Phòng y tế")
                    .createdAt(LocalDateTime.now()) // @CreationTimestamp sẽ xử lý
                    .updatedAt(LocalDateTime.now()) // @UpdateTimestamp sẽ xử lý
                    .updatedByUser(nurse)
                    .deleted(false)
                    .build();
            HealthIncident savedIncident1 = healthIncidentRepository.save(incident1);

            // Sử dụng Paracetamol
            if (paracetamolOpt.isPresent()) {
                MedicalSupply paracetamol = paracetamolOpt.get();
                try {
                    medicalSupplyService.recordSupplyUsageForIncident(paracetamol, 1, savedIncident1, nurse);
                    log.info("Đã ghi nhận sử dụng Paracetamol cho sự cố ID {}.", savedIncident1.getIncidentId());
                } catch (InvalidOperationException e) {
                    log.warn("Không thể ghi nhận sử dụng Paracetamol cho sự cố ID {}: {}", savedIncident1.getIncidentId(), e.getMessage());
                }
            } else {
                log.warn("Không tìm thấy vật tư Paracetamol để ghi nhận sử dụng.");
            }
            log.info("Đã tạo sự cố Sốt nhẹ cho học sinh {}.", student.getFullName());

            // Sự cố 2: Trầy xước nhẹ
            HealthIncident incident2 = HealthIncident.builder()
                    .student(student)
                    .recordedByUser(nurse)
                    .incidentDateTime(LocalDateTime.now().minusDays(1).withHour(10).withMinute(30)) // Hôm qua lúc 10:30
                    .incidentType(HealthIncidentType.MINOR_INJURY)
                    .description("Học sinh bị ngã trong giờ ra chơi, trầy xước nhẹ ở đầu gối phải.")
                    .actionTaken("Rửa vết thương bằng nước muối sinh lý, sát khuẩn và băng bó bằng băng gạc y tế.")
                    .location("Sân trường")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .updatedByUser(nurse)
                    .deleted(false)
                    .build();
            HealthIncident savedIncident2 = healthIncidentRepository.save(incident2);

            // Sử dụng băng gạc
            if (bandageOpt.isPresent()) {
                MedicalSupply bandage = bandageOpt.get();
                try {
                    medicalSupplyService.recordSupplyUsageForIncident(bandage, 1, savedIncident2, nurse);
                    log.info("Đã ghi nhận sử dụng Băng gạc y tế cho sự cố ID {}.", savedIncident2.getIncidentId());
                } catch (InvalidOperationException e) {
                    log.warn("Không thể ghi nhận sử dụng Băng gạc y tế cho sự cố ID {}: {}", savedIncident2.getIncidentId(), e.getMessage());
                }
            } else {
                log.warn("Không tìm thấy vật tư Băng gạc y tế để ghi nhận sử dụng.");
            }
            log.info("Đã tạo sự cố Trầy xước cho học sinh {}.", student.getFullName());

        } else {
            log.info("Sự cố sức khỏe cho học sinh {} có thể đã tồn tại.", student.getFullName());
        }

        log.info("Hoàn tất khởi tạo dữ liệu Sự cố Sức khỏe.");
    }
}