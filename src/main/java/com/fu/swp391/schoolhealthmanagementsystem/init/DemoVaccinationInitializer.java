package com.fu.swp391.schoolhealthmanagementsystem.init;

import com.fu.swp391.schoolhealthmanagementsystem.entity.PostVaccinationMonitoring;
import com.fu.swp391.schoolhealthmanagementsystem.entity.SchoolVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationConsent;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ClassGroup;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import com.fu.swp391.schoolhealthmanagementsystem.repository.PostVaccinationMonitoringRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.SchoolVaccinationRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.UserRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.VaccinationCampaignRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.VaccinationConsentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Order(6) // Ensure it runs after User and Student initializers
public class DemoVaccinationInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final VaccinationCampaignRepository vaccinationCampaignRepository;
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final SchoolVaccinationRepository schoolVaccinationRepository;
    private final PostVaccinationMonitoringRepository postVaccinationMonitoringRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createVaccinationData();
    }

    private void createVaccinationData() {
        // 1. Find the nurse user to organize the campaign
        Optional<User> nurseOptional = userRepository.findByEmail("nurse@school.com");
        if (nurseOptional.isEmpty()) {
            // If no nurse, maybe find an admin
            nurseOptional =
                    userRepository.findAll().stream()
                            .filter(u -> u.getRole() == UserRole.MedicalStaff || u.getRole() == UserRole.SchoolAdmin)
                            .findFirst();
        }
        if (nurseOptional.isEmpty()) {
            System.out.println("Cannot find a nurse or admin to create vaccination campaign.");
            return;
        }
        User nurse = nurseOptional.get();

        // 2. Create a Vaccination Campaign if it doesn't exist
        if (vaccinationCampaignRepository.count() == 0) {
            VaccinationCampaign campaign =
                    VaccinationCampaign.builder()
                            .campaignName("Đợt tiêm chủng vắc xin Sởi-Quai bị-Rubella (MMR) đợt 1 năm 2025")
                            .vaccineName("MMR II")
                            .description("Tiêm chủng cho trẻ để phòng ngừa 3 bệnh: sởi, quai bị và rubella.")
                            .vaccinationDate(LocalDate.now().plusMonths(1))
                            .consentDeadline(LocalDate.now().plusWeeks(2))
                            .targetClassGroup(ClassGroup.CHOI)
                            .status(VaccinationCampaignStatus.SCHEDULED)
                            .organizedByUser(nurse).organizedByUser(nurse)
                            .healthcareProviderName("Trung tâm y tế dự phòng quận 9")
                            .build();
            vaccinationCampaignRepository.save(campaign);

            // 3. Find students in target groups and create consent forms
            List<Student> students =
                    studentRepository.findByClassGroupIn(List.of(ClassGroup.CHOI, ClassGroup.LA));
            for (Student student : students) {
                // Find parent
                student.getParentLinks().stream()
                        .findFirst()
                        .ifPresent(
                                link -> {
                                    User parent = link.getParent();
                                    VaccinationConsent consent =
                                            VaccinationConsent.builder()
                                                    .campaign(campaign)
                                                    .student(student)
                                                    .parent(parent)
                                                    .status(ConsentStatus.PENDING) // Initially pending
                                                    .consentFormSentAt(LocalDateTime.now())
                                                    .build();
                                    vaccinationConsentRepository.save(consent);
                                });
            }

            // 4. Simulate some parent responses
            List<VaccinationConsent> consents = vaccinationConsentRepository.findAll();
            for (int i = 0; i < consents.size(); i++) {
                VaccinationConsent consent = consents.get(i);
                if (i % 5 == 0) { // Simulate refusal for every 5th student
                    consent.setStatus(ConsentStatus.DECLINED);
                    consent.setParentResponse("Gia đình đã tiêm mũi này cho bé ở ngoài.");
                    consent.setResponseReceivedAt(LocalDateTime.now().plusDays(2));
                } else { // Simulate approval for others
                    consent.setStatus(ConsentStatus.APPROVED);
                    consent.setParentResponse("Đồng ý cho bé tiêm.");
                    consent.setResponseReceivedAt(LocalDateTime.now().plusDays(1));
                    if (i % 10 == 0) { // Add some medical notes
                        consent.setMedicalNotes("Bé bị dị ứng nhẹ với trứng gà.");
                    }
                }
                vaccinationConsentRepository.save(consent);
            }

            // 5. Create SchoolVaccination records for approved consents
            List<VaccinationConsent> approvedConsents =
                    vaccinationConsentRepository.findByStatus(ConsentStatus.APPROVED);
            for (VaccinationConsent consent : approvedConsents) {
                SchoolVaccination schoolVaccination =
                        SchoolVaccination.builder()
                                .campaign(campaign)
                                .student(consent.getStudent())
                                .consent(consent)
                                .status(SchoolVaccinationStatus.SCHEDULED)
                                .vaccinationDate(campaign.getVaccinationDate())
                                .administeredByUser(nurse) // Initially assigned to the nurse
                                .build();
                schoolVaccinationRepository.save(schoolVaccination);
            }

            // 6. Simulate some vaccinations being completed and monitored
            List<SchoolVaccination> scheduledVaccinations =
                    schoolVaccinationRepository.findByStatus(SchoolVaccinationStatus.SCHEDULED);
            for (int i = 0; i < scheduledVaccinations.size() && i < 5; i++) { // Simulate for first 5
                SchoolVaccination vaccination = scheduledVaccinations.get(i);
                vaccination.setStatus(SchoolVaccinationStatus.COMPLETED);
                vaccination.setNotes("Bé hợp tác tốt, không quấy khóc.");
                schoolVaccinationRepository.save(vaccination);

                // Create monitoring record
                PostVaccinationMonitoring monitoring =
                        PostVaccinationMonitoring.builder()
                                .schoolVaccination(vaccination)
                                .monitoringTime(LocalDateTime.now().plusMonths(1).plusMinutes(30))
                                .temperature(37.2f)
                                .hasSideEffects(false)
                                .notes("Không có biểu hiện bất thường sau tiêm 30 phút.")
                                .recordedByUser(nurse)
                                .build();
                postVaccinationMonitoringRepository.save(monitoring);
            }
        }
    }
}

