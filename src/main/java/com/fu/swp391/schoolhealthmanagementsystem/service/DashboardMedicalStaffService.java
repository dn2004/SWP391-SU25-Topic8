package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.DashboardMedicalStaffDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardMedicalStaffService {
    private final StudentRepository studentRepository;
    private final HealthIncidentRepository healthIncidentRepository;
    private final StudentMedicationRepository studentMedicationRepository;
    private final ScheduledMedicationTaskRepository scheduledMedicationTaskRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final StudentChronicDiseaseRepository studentChronicDiseaseRepository;
    private final VaccinationCampaignRepository vaccinationCampaignRepository;
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final SchoolVaccinationRepository schoolVaccinationRepository;
    private final PostVaccinationMonitoringRepository postVaccinationMonitoringRepository;
    private final NotificationRepository notificationRepository;
    private final MedicationTimeSlotRepository medicationTimeSlotRepository;

    public DashboardMedicalStaffDto getMedicalStaffDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Daily Care Overview
        long totalStudentsUnderCare = studentRepository.countByStatus(StudentStatus.ACTIVE);

        Map<ClassGroup, Long> studentsByClassGroup = new EnumMap<>(ClassGroup.class);
        for (ClassGroup classGroup : ClassGroup.values()) {
            studentsByClassGroup.put(classGroup, studentRepository.countByClassGroup(classGroup));
        }

        Map<Class, Long> studentsByClass = new EnumMap<>(Class.class);
        for (Class classValue : Class.values()) {
            studentsByClass.put(classValue, studentRepository.countByClassValue(classValue));
        }

        // Today's Tasks & Schedules (Note: would need additional repository methods for date filtering)
        long todayScheduledMedicationTasks = 0; // Placeholder - need countByScheduledDate method
        long pendingMedicationTasks = scheduledMedicationTaskRepository.countByStatus(ScheduledMedicationTaskStatus.SCHEDULED);
        long completedMedicationTasksToday = 0; // Placeholder - need countByStatusAndAdministeredAtAfter method

        Map<ScheduledMedicationTaskStatus, Long> medicationTasksByStatus = new EnumMap<>(ScheduledMedicationTaskStatus.class);
        for (ScheduledMedicationTaskStatus status : ScheduledMedicationTaskStatus.values()) {
            medicationTasksByStatus.put(status, scheduledMedicationTaskRepository.countByStatus(status));
        }

        long todayVaccinationSchedules = 0; // Placeholder - need countByVaccinationDate method
        long completedVaccinationsToday = 0; // Placeholder - need countByStatusAndVaccinationDate method

        // Recent Health Incidents
        long recentHealthIncidents = healthIncidentRepository.countByCreatedAtAfter(sevenDaysAgo);
        long todayHealthIncidents = healthIncidentRepository.countByCreatedAtAfter(todayStart);

        Map<HealthIncidentType, Long> recentIncidentsByType = new EnumMap<>(HealthIncidentType.class);
        for (HealthIncidentType type : HealthIncidentType.values()) {
            recentIncidentsByType.put(type, healthIncidentRepository.countByIncidentType(type));
        }

        // Student Medications Management
        long totalActiveMedications = studentMedicationRepository.countByStatus(MedicationStatus.AVAILABLE);
        long medicationsNeedingAttention = studentMedicationRepository.countByStatus(MedicationStatus.OUT_OF_DOSES) +
                                         studentMedicationRepository.countByStatus(MedicationStatus.EXPIRED);
        long expiredMedications = studentMedicationRepository.countByStatus(MedicationStatus.EXPIRED);

        Map<MedicationStatus, Long> medicationsByStatus = new EnumMap<>(MedicationStatus.class);
        for (MedicationStatus status : MedicationStatus.values()) {
            medicationsByStatus.put(status, studentMedicationRepository.countByStatus(status));
        }

        // Medical Supply Status
        long totalMedicalSupplies = medicalSupplyRepository.count();
        long lowStockSupplies = medicalSupplyRepository.countByCurrentStockLessThanEqual(5);
        long expiredSupplies = medicalSupplyRepository.countByStatus(MedicalSupplyStatus.EXPIRED);
        long criticalSupplies = medicalSupplyRepository.countByCurrentStockLessThanEqual(2); // Extremely low

        Map<MedicalSupplyStatus, Long> suppliesByStatus = new EnumMap<>(MedicalSupplyStatus.class);
        for (MedicalSupplyStatus status : MedicalSupplyStatus.values()) {
            suppliesByStatus.put(status, medicalSupplyRepository.countByStatus(status));
        }

        // Student Health Records
        long totalChronicDiseaseStudents = studentChronicDiseaseRepository.countByStatus(StudentChronicDiseaseStatus.APPROVE);
        long chronicDiseaseNeedingMonitoring = studentChronicDiseaseRepository.countByStatus(StudentChronicDiseaseStatus.PENDING);

        Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus = new EnumMap<>(StudentChronicDiseaseStatus.class);
        for (StudentChronicDiseaseStatus status : StudentChronicDiseaseStatus.values()) {
            chronicDiseasesByStatus.put(status, studentChronicDiseaseRepository.countByStatus(status));
        }

        // Vaccination Management
        long upcomingVaccinationCampaigns = vaccinationCampaignRepository.countByStatus(VaccinationCampaignStatus.SCHEDULED);
        long activeVaccinationCampaigns = vaccinationCampaignRepository.countByStatus(VaccinationCampaignStatus.IN_PROGRESS);

        Map<VaccinationCampaignStatus, Long> campaignsByStatus = new EnumMap<>(VaccinationCampaignStatus.class);
        for (VaccinationCampaignStatus status : VaccinationCampaignStatus.values()) {
            campaignsByStatus.put(status, vaccinationCampaignRepository.countByStatus(status));
        }

        long pendingVaccinationConsents = vaccinationConsentRepository.countByStatus(ConsentStatus.PENDING);

        Map<ConsentStatus, Long> consentsByStatus = new EnumMap<>(ConsentStatus.class);
        for (ConsentStatus status : ConsentStatus.values()) {
            consentsByStatus.put(status, vaccinationConsentRepository.countByStatus(status));
        }

        // Post-Vaccination Monitoring
        long totalPostVaccinationMonitoring = postVaccinationMonitoringRepository.count();
        long monitoringWithSideEffects = postVaccinationMonitoringRepository.countByHasSideEffectsTrue();
        long todayMonitoringDue = 0; // Placeholder - need countByMonitoringTimeDate method

        // Workload Distribution
        long totalNotifications = notificationRepository.count();
        long unreadNotifications = notificationRepository.countByReadFalse();
        long tasksAssignedToMe = 0; // Placeholder - need current user context
        long tasksCompletedByMeToday = 0; // Placeholder - need current user context

        // Time Slot Management
        long morningMedicationSlots = medicationTimeSlotRepository.countBySchoolSessionHint(SchoolSession.MORNING);
        long afternoonMedicationSlots = medicationTimeSlotRepository.countBySchoolSessionHint(SchoolSession.AFTERNOON);

        Map<SchoolSession, Long> medicationTimeSlotsBySession = new EnumMap<>(SchoolSession.class);
        for (SchoolSession session : SchoolSession.values()) {
            medicationTimeSlotsBySession.put(session, medicationTimeSlotRepository.countBySchoolSessionHint(session));
        }

        return new DashboardMedicalStaffDto(
            totalStudentsUnderCare,
            studentsByClassGroup,
            studentsByClass,
            todayScheduledMedicationTasks,
            pendingMedicationTasks,
            completedMedicationTasksToday,
            medicationTasksByStatus,
            todayVaccinationSchedules,
            completedVaccinationsToday,
            recentHealthIncidents,
            todayHealthIncidents,
            recentIncidentsByType,
            totalActiveMedications,
            medicationsNeedingAttention,
            expiredMedications,
            medicationsByStatus,
            totalMedicalSupplies,
            lowStockSupplies,
            expiredSupplies,
            criticalSupplies,
            suppliesByStatus,
            totalChronicDiseaseStudents,
            chronicDiseaseNeedingMonitoring,
            chronicDiseasesByStatus,
            upcomingVaccinationCampaigns,
            activeVaccinationCampaigns,
            campaignsByStatus,
            pendingVaccinationConsents,
            consentsByStatus,
            totalPostVaccinationMonitoring,
            monitoringWithSideEffects,
            todayMonitoringDue,
            totalNotifications,
            unreadNotifications,
            tasksAssignedToMe,
            tasksCompletedByMeToday,
            morningMedicationSlots,
            afternoonMedicationSlots,
            medicationTimeSlotsBySession
        );
    }
}
