package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(6) // Ensure it runs after User and Student initializers
public class DemoVaccinationInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final VaccinationCampaignRepository vaccinationCampaignRepository;
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final SchoolVaccinationRepository schoolVaccinationRepository;
    private final PostVaccinationMonitoringRepository postVaccinationMonitoringRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        log.info("Bắt đầu khởi tạo dữ liệu Tiêm chủng...");
        createVaccinationData();
        log.info("Hoàn tất khởi tạo dữ liệu Tiêm chủng.");
    }

    private void createVaccinationData() {
        // 1. Find a nurse user to organize the campaign
        Optional<User> nurseOptional = userRepository.findByEmail("nurse1@example.com");
        if (nurseOptional.isEmpty()) {
            // If no specific nurse, find any medical staff
            nurseOptional = userRepository.findAll().stream()
                    .filter(u -> u.getRole() == UserRole.MedicalStaff)
                    .findFirst();
        }
        if (nurseOptional.isEmpty()) {
            log.warn("Không tìm thấy nhân viên y tế để tạo chiến dịch tiêm chủng. Bỏ qua.");
            return;
        }
        User nurse = nurseOptional.get();

        // 2. Create a Vaccination Campaign if it doesn't exist
        if (vaccinationCampaignRepository.count() > 0) {
            log.info("Dữ liệu chiến dịch tiêm chủng đã tồn tại. Bỏ qua.");
            return;
        }

        VaccinationCampaign campaign = VaccinationCampaign.builder()
                .campaignName("Đợt tiêm chủng vắc xin Sởi-Quai bị-Rubella (MMR) đợt 1 năm 2025")
                .vaccineName("MMR II")
                .description("Tiêm chủng cho trẻ để phòng ngừa 3 bệnh: sởi, quai bị và rubella.")
                .vaccinationDate(LocalDate.now().plusMonths(1))
                .consentDeadline(LocalDate.now().plusWeeks(2))
                .targetClassGroup(ClassGroup.CHOI)
                .status(VaccinationCampaignStatus.SCHEDULED)
                .organizedByUser(nurse)
                .healthcareProviderName("Trung tâm y tế dự phòng quận Thủ Đức")
                .build();
        vaccinationCampaignRepository.save(campaign);
        log.info("Đã tạo chiến dịch: {}", campaign.getCampaignName());

        // 3. Find students in target groups and create consent forms
        List<Student> students = studentRepository.findByClassGroupIn(List.of(ClassGroup.CHOI, ClassGroup.LA));
        for (Student student : students) {
            student.getParentLinks().stream().findFirst().ifPresent(link -> {
                User parent = link.getParent();
                if (!vaccinationConsentRepository.existsByCampaignAndStudent(campaign, student)) {
                    VaccinationConsent consent = VaccinationConsent.builder()
                            .campaign(campaign)
                            .student(student)
                            .parent(parent)
                            .status(ConsentStatus.PENDING)
                            .consentFormSentAt(LocalDateTime.now())
                            .build();
                    vaccinationConsentRepository.save(consent);
                }
            });
        }
        log.info("Đã tạo {} phiếu chấp thuận cho chiến dịch.", students.size());

        // 4. Simulate some parent responses
        List<VaccinationConsent> consents = vaccinationConsentRepository.findAllByCampaign(campaign);
        int approvedCount = 0;
        for (int i = 0; i < consents.size(); i++) {
            VaccinationConsent consent = consents.get(i);
            if (i % 5 == 0) { // Simulate refusal
                consent.setStatus(ConsentStatus.DECLINED);
                consent.setParentResponse("Gia đình đã tiêm mũi này cho bé ở ngoài.");
                consent.setResponseReceivedAt(LocalDateTime.now().plusDays(2));
            } else { // Simulate approval
                consent.setStatus(ConsentStatus.APPROVED);
                consent.setParentResponse("Đồng ý cho bé tiêm.");
                consent.setResponseReceivedAt(LocalDateTime.now().plusDays(1));
                if (i % 10 == 0) {
                    consent.setMedicalNotes("Bé bị dị ứng nhẹ với trứng gà.");
                }
                approvedCount++;
            }
            vaccinationConsentRepository.save(consent);
        }
        log.info("Đã mô phỏng phản hồi cho các phiếu chấp thuận. {} phiếu được chấp thuận.", approvedCount);

        // 5. Create SchoolVaccination records for approved consents
        List<VaccinationConsent> approvedConsents = vaccinationConsentRepository.findByCampaignAndStatus(campaign, ConsentStatus.APPROVED);
        for (VaccinationConsent consent : approvedConsents) {
            SchoolVaccination schoolVaccination = SchoolVaccination.builder()
                    .campaign(campaign)
                    .student(consent.getStudent())
                    .consent(consent)
                    .status(SchoolVaccinationStatus.SCHEDULED)
                    .vaccinationDate(campaign.getVaccinationDate())
                    .administeredByUser(nurse)
                    .build();
            schoolVaccinationRepository.save(schoolVaccination);
        }
        log.info("Đã lên lịch tiêm cho {} học sinh.", approvedConsents.size());

        // 6. Simulate some vaccinations being completed and monitored
        List<SchoolVaccination> scheduledVaccinations = schoolVaccinationRepository.findByCampaignAndStatus(campaign, SchoolVaccinationStatus.SCHEDULED);
        int completedCount = 0;
        for (int i = 0; i < scheduledVaccinations.size() && i < 15; i++) { // Simulate for first 15
            SchoolVaccination vaccination = scheduledVaccinations.get(i);
            vaccination.setStatus(SchoolVaccinationStatus.COMPLETED);
            vaccination.setNotes("Bé hợp tác tốt, không quấy khóc.");
            schoolVaccinationRepository.save(vaccination);

            PostVaccinationMonitoring monitoring = PostVaccinationMonitoring.builder()
                    .schoolVaccination(vaccination)
                    .monitoringTime(vaccination.getVaccinationDate().atTime(10, 30).plusMinutes(30))
                    .temperature(37.2f)
                    .hasSideEffects(false)
                    .notes("Không có biểu hiện bất thường sau tiêm 30 phút.")
                    .recordedByUser(nurse)
                    .build();
            postVaccinationMonitoringRepository.save(monitoring);
            completedCount++;
        }
        if (completedCount > 0) {
            log.info("Đã mô phỏng hoàn thành và theo dõi sau tiêm cho {} học sinh.", completedCount);
        }
    }
}