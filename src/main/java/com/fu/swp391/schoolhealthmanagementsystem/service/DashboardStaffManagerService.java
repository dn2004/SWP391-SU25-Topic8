package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard.DashboardStaffManagerDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardStaffManagerService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final HealthIncidentRepository healthIncidentRepository;
    private final BlogRepository blogRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final VaccinationCampaignRepository vaccinationCampaignRepository;
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final SchoolVaccinationRepository schoolVaccinationRepository;
    private final StudentChronicDiseaseRepository studentChronicDiseaseRepository;
    private final StudentVaccinationRepository studentVaccinationRepository;
    private final StudentMedicationRepository studentMedicationRepository;
    private final ScheduledMedicationTaskRepository scheduledMedicationTaskRepository;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final PostVaccinationMonitoringRepository postVaccinationMonitoringRepository;
    private final MedicationTimeSlotRepository medicationTimeSlotRepository;

    public DashboardStaffManagerDto getStaffManagerDashboard() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        // User & Staff Management Statistics
        long totalUsers = userRepository.count();
        long totalMedicalStaff = userRepository.countByRole(UserRole.MedicalStaff);
        long totalParents = userRepository.countByRole(UserRole.Parent);
        long totalSchoolAdmins = userRepository.countByRole(UserRole.SchoolAdmin);
        long activeUsers = userRepository.countByActive(true);
        long inactiveUsers = userRepository.countByActive(false);

        Map<UserRole, Long> usersByRole = new EnumMap<>(UserRole.class);
        for (UserRole role : UserRole.values()) {
            usersByRole.put(role, userRepository.countByRole(role));
        }

        // Student Statistics (for staff oversight)
        long totalStudents = studentRepository.count();

        Map<ClassGroup, Long> studentsByClassGroup = new EnumMap<>(ClassGroup.class);
        for (ClassGroup classGroup : ClassGroup.values()) {
            studentsByClassGroup.put(classGroup, studentRepository.countByClassGroup(classGroup));
        }

        Map<StudentStatus, Long> studentsByStatus = new EnumMap<>(StudentStatus.class);
        for (StudentStatus status : StudentStatus.values()) {
            studentsByStatus.put(status, studentRepository.countByStatus(status));
        }

        // Daily Operations - Health Incidents
        long totalHealthIncidents = healthIncidentRepository.count();
        long recentHealthIncidents = healthIncidentRepository.countByCreatedAtAfter(sevenDaysAgo);
        long todayHealthIncidents = healthIncidentRepository.countByCreatedAtAfter(todayStart);

        Map<HealthIncidentType, Long> incidentsByType = new EnumMap<>(HealthIncidentType.class);
        for (HealthIncidentType type : HealthIncidentType.values()) {
            incidentsByType.put(type, healthIncidentRepository.countByIncidentType(type));
        }

        // Medical Supply Management
        long totalMedicalSupplies = medicalSupplyRepository.count();
        long lowStockSupplies = medicalSupplyRepository.countByCurrentStockLessThanEqual(5);
        long expiredSupplies = medicalSupplyRepository.countByStatus(MedicalSupplyStatus.EXPIRED);

        Map<MedicalSupplyStatus, Long> suppliesByStatus = new EnumMap<>(MedicalSupplyStatus.class);
        for (MedicalSupplyStatus status : MedicalSupplyStatus.values()) {
            suppliesByStatus.put(status, medicalSupplyRepository.countByStatus(status));
        }

        long totalSupplyTransactions = supplyTransactionRepository.count();
        // Note: We would need to add countByTransactionDateTimeAfter method to get today's transactions
        long todaySupplyTransactions = 0; // Placeholder - would need repository method

        Map<SupplyTransactionType, Long> supplyTransactionsByType = new EnumMap<>(SupplyTransactionType.class);
        for (SupplyTransactionType type : SupplyTransactionType.values()) {
            supplyTransactionsByType.put(type, supplyTransactionRepository.countBySupplyTransactionType(type));
        }

        // Medication Management
        long totalStudentMedications = studentMedicationRepository.count();
        long activeMedications = studentMedicationRepository.countByStatus(MedicationStatus.AVAILABLE);

        Map<MedicationStatus, Long> medicationsByStatus = new EnumMap<>(MedicationStatus.class);
        for (MedicationStatus status : MedicationStatus.values()) {
            medicationsByStatus.put(status, studentMedicationRepository.countByStatus(status));
        }

        long totalScheduledMedicationTasks = scheduledMedicationTaskRepository.count();
        // Note: We would need to add methods to count today's tasks
        long todayScheduledTasks = 0; // Placeholder
        long pendingMedicationTasks = scheduledMedicationTaskRepository.countByStatus(ScheduledMedicationTaskStatus.SCHEDULED);
        long completedMedicationTasks = scheduledMedicationTaskRepository.countByStatus(ScheduledMedicationTaskStatus.ADMINISTERED);

        Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus = new EnumMap<>(ScheduledMedicationTaskStatus.class);
        for (ScheduledMedicationTaskStatus status : ScheduledMedicationTaskStatus.values()) {
            scheduledTasksByStatus.put(status, scheduledMedicationTaskRepository.countByStatus(status));
        }

        // Vaccination Management
        long totalVaccinationCampaigns = vaccinationCampaignRepository.count();
        long activeVaccinationCampaigns = vaccinationCampaignRepository.countByStatus(VaccinationCampaignStatus.IN_PROGRESS);

        Map<VaccinationCampaignStatus, Long> campaignsByStatus = new EnumMap<>(VaccinationCampaignStatus.class);
        for (VaccinationCampaignStatus status : VaccinationCampaignStatus.values()) {
            campaignsByStatus.put(status, vaccinationCampaignRepository.countByStatus(status));
        }

        long totalVaccinationConsents = vaccinationConsentRepository.count();
        long pendingConsents = vaccinationConsentRepository.countByStatus(ConsentStatus.PENDING);

        Map<ConsentStatus, Long> consentsByStatus = new EnumMap<>(ConsentStatus.class);
        for (ConsentStatus status : ConsentStatus.values()) {
            consentsByStatus.put(status, vaccinationConsentRepository.countByStatus(status));
        }

        long totalSchoolVaccinations = schoolVaccinationRepository.count();

        Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus = new EnumMap<>(SchoolVaccinationStatus.class);
        for (SchoolVaccinationStatus status : SchoolVaccinationStatus.values()) {
            schoolVaccinationsByStatus.put(status, schoolVaccinationRepository.countByStatus(status));
        }

        // Health Records Management
        long totalChronicDiseaseRecords = studentChronicDiseaseRepository.count();
        long pendingChronicDiseaseRecords = studentChronicDiseaseRepository.countByStatus(StudentChronicDiseaseStatus.PENDING);

        Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus = new EnumMap<>(StudentChronicDiseaseStatus.class);
        for (StudentChronicDiseaseStatus status : StudentChronicDiseaseStatus.values()) {
            chronicDiseasesByStatus.put(status, studentChronicDiseaseRepository.countByStatus(status));
        }

        long totalStudentVaccinations = studentVaccinationRepository.count();
        long pendingStudentVaccinations = studentVaccinationRepository.countByStatus(StudentVaccinationStatus.PENDING);

        Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus = new EnumMap<>(StudentVaccinationStatus.class);
        for (StudentVaccinationStatus status : StudentVaccinationStatus.values()) {
            studentVaccinationsByStatus.put(status, studentVaccinationRepository.countByStatus(status));
        }

        // Parent-Student Links Management
        long totalParentStudentLinks = parentStudentLinkRepository.count();
        long activeParentStudentLinks = parentStudentLinkRepository.countByStatus(LinkStatus.ACTIVE);

        Map<LinkStatus, Long> parentStudentLinksByStatus = new EnumMap<>(LinkStatus.class);
        for (LinkStatus status : LinkStatus.values()) {
            parentStudentLinksByStatus.put(status, parentStudentLinkRepository.countByStatus(status));
        }

        Map<RelationshipType, Long> parentStudentLinksByRelationship = new EnumMap<>(RelationshipType.class);
        for (RelationshipType type : RelationshipType.values()) {
            parentStudentLinksByRelationship.put(type, parentStudentLinkRepository.countByRelationshipType(type));
        }

        // Content Management
        long totalBlogs = blogRepository.count();
        long recentBlogs = blogRepository.countByCreatedAtAfter(thirtyDaysAgo);

        Map<BlogStatus, Long> blogsByStatus = new EnumMap<>(BlogStatus.class);
        for (BlogStatus status : BlogStatus.values()) {
            blogsByStatus.put(status, blogRepository.countByStatus(status));
        }

        Map<BlogCategory, Long> blogsByCategory = new EnumMap<>(BlogCategory.class);
        for (BlogCategory category : BlogCategory.values()) {
            blogsByCategory.put(category, blogRepository.countByCategory(category));
        }

        // System Activity & Monitoring
        long totalNotifications = notificationRepository.count();
        long unreadNotifications = notificationRepository.countByReadFalse();
        long totalPostVaccinationMonitoring = postVaccinationMonitoringRepository.count();
        long monitoringWithSideEffects = postVaccinationMonitoringRepository.countByHasSideEffectsTrue();

        // Time Management
        long totalMedicationTimeSlots = medicationTimeSlotRepository.count();

        Map<SchoolSession, Long> medicationTimeSlotsBySession = new EnumMap<>(SchoolSession.class);
        for (SchoolSession session : SchoolSession.values()) {
            medicationTimeSlotsBySession.put(session, medicationTimeSlotRepository.countBySchoolSessionHint(session));
        }

        return new DashboardStaffManagerDto(
            totalUsers,
            totalMedicalStaff,
            totalParents,
            totalSchoolAdmins,
            activeUsers,
            inactiveUsers,
            usersByRole,
            totalStudents,
            studentsByClassGroup,
            studentsByStatus,
            totalHealthIncidents,
            recentHealthIncidents,
            todayHealthIncidents,
            incidentsByType,
            totalMedicalSupplies,
            lowStockSupplies,
            expiredSupplies,
            suppliesByStatus,
            totalSupplyTransactions,
            todaySupplyTransactions,
            supplyTransactionsByType,
            totalStudentMedications,
            activeMedications,
            medicationsByStatus,
            totalScheduledMedicationTasks,
            todayScheduledTasks,
            pendingMedicationTasks,
            completedMedicationTasks,
            scheduledTasksByStatus,
            totalVaccinationCampaigns,
            activeVaccinationCampaigns,
            campaignsByStatus,
            totalVaccinationConsents,
            pendingConsents,
            consentsByStatus,
            totalSchoolVaccinations,
            schoolVaccinationsByStatus,
            totalChronicDiseaseRecords,
            pendingChronicDiseaseRecords,
            chronicDiseasesByStatus,
            totalStudentVaccinations,
            pendingStudentVaccinations,
            studentVaccinationsByStatus,
            totalParentStudentLinks,
            activeParentStudentLinks,
            parentStudentLinksByStatus,
            parentStudentLinksByRelationship,
            totalBlogs,
            recentBlogs,
            blogsByStatus,
            blogsByCategory,
            totalNotifications,
            unreadNotifications,
            totalPostVaccinationMonitoring,
            monitoringWithSideEffects,
            totalMedicationTimeSlots,
            medicationTimeSlotsBySession
        );
    }
}
