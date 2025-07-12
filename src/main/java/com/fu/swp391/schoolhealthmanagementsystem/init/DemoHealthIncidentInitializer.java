package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import com.fu.swp391.schoolhealthmanagementsystem.service.MedicalSupplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(5) // Chạy sau các Initializer khác
public class DemoHealthIncidentInitializer implements ApplicationRunner {

    private final HealthIncidentRepository healthIncidentRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final MedicalSupplyService medicalSupplyService;

    private record IncidentTemplate(String description, HealthIncidentType type, String actionTaken, String location) {}

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Bắt đầu khởi tạo dữ liệu Sự cố Sức khỏe...");

        if (healthIncidentRepository.count() > 0) {
            log.info("Dữ liệu sự cố sức khỏe đã tồn tại. Bỏ qua khởi tạo.");
            return;
        }

        List<Student> students = studentRepository.findAll();
        List<User> nurses = userRepository.findAllByRole(UserRole.MedicalStaff);
        List<MedicalSupply> supplies = medicalSupplyRepository.findAll();

        if (students.isEmpty() || nurses.isEmpty() || supplies.isEmpty()) {
            log.warn("Không đủ dữ liệu (học sinh, y tá, hoặc vật tư) để tạo sự cố. Bỏ qua.");
            return;
        }

        List<IncidentTemplate> templates = List.of(
            new IncidentTemplate("Học sinh cảm thấy mệt mỏi, nhiệt độ 37.8°C.", HealthIncidentType.ILLNESS, "Cho nghỉ ngơi, theo dõi nhiệt độ.", "Phòng y tế"),
            new IncidentTemplate("Bị ngã trong giờ ra chơi, trầy xước nhẹ ở đầu gối.", HealthIncidentType.MINOR_INJURY, "Rửa vết thương, sát khuẩn và băng bó.", "Sân trường"),
            new IncidentTemplate("Đau bụng nhẹ sau bữa ăn trưa.", HealthIncidentType.ILLNESS, "Cho uống nước ấm, nghỉ ngơi.", "Phòng y tế"),
            new IncidentTemplate("Bị côn trùng cắn ở tay, sưng đỏ.", HealthIncidentType.MINOR_INJURY, "Bôi thuốc chống dị ứng.", "Sân sau"),
            new IncidentTemplate("Chảy máu cam nhẹ.", HealthIncidentType.ILLNESS, "Yêu cầu học sinh ngồi yên, đầu hơi cúi về phía trước.", "Lớp học"),
            new IncidentTemplate("Nói rằng cảm thấy đau đầu.", HealthIncidentType.ILLNESS, "Đo nhiệt độ, cho nằm nghỉ.", "Phòng y tế")
        );

        Random random = new Random();
        List<Student> studentPool = new ArrayList<>(students);
        Collections.shuffle(studentPool);

        int incidentsCreated = 0;
        int studentIndex = 0;

        while (incidentsCreated < 60 && !studentPool.isEmpty()) {
            if (studentIndex >= studentPool.size()) {
                studentIndex = 0; // Quay lại từ đầu nếu đã duyệt hết danh sách
            }

            Student student = studentPool.get(studentIndex);

            long incidentCount = healthIncidentRepository.countByStudent(student);
            if (incidentCount >= 2) {
                studentPool.remove(studentIndex); // Xóa học sinh đã đủ 2 sự cố
                continue; // Chuyển sang học sinh tiếp theo
            }

            User nurse = nurses.get(random.nextInt(nurses.size()));
            IncidentTemplate template = templates.get(random.nextInt(templates.size()));
            MedicalSupply supplyUsed = supplies.get(random.nextInt(supplies.size()));

            LocalDateTime incidentTime = LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(24));

            HealthIncident incident = HealthIncident.builder()
                    .student(student)
                    .recordedByUser(nurse)
                    .incidentDateTime(incidentTime)
                    .incidentType(template.type())
                    .description(template.description())
                    .actionTaken(template.actionTaken())
                    .location(template.location())
                    .updatedByUser(nurse)
                    .deleted(false)
                    .build();

            HealthIncident savedIncident = healthIncidentRepository.save(incident);

            // Ghi nhận sử dụng vật tư
            try {
                if (supplyUsed.getCurrentStock() > 0) {
                    medicalSupplyService.recordSupplyUsageForIncident(supplyUsed, 1, savedIncident, nurse);
                }
            } catch (InvalidOperationException e) {
                log.warn("Không thể ghi nhận sử dụng vật tư cho sự cố ID {}: {}", savedIncident.getIncidentId(), e.getMessage());
            }

            incidentsCreated++;
            studentIndex++;
        }

        log.info("Hoàn tất khởi tạo {} sự cố sức khỏe.", incidentsCreated);
    }
}
