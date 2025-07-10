package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard.DashboardAdminDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardAdminService {
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
    private final StudentMedicationTransactionRepository studentMedicationTransactionRepository;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final NotificationRepository notificationRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final PostVaccinationMonitoringRepository postVaccinationMonitoringRepository;
    private final MedicationTimeSlotRepository medicationTimeSlotRepository;

    public DashboardAdminDto getAdminDashboard() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // User & Student Statistics
        long totalStudents = studentRepository.count();
        long totalParents = userRepository.countByRole(UserRole.Parent);
        long totalMedicalStaff = userRepository.countByRole(UserRole.MedicalStaff);
        long totalStaffManagers = userRepository.countByRole(UserRole.StaffManager);
        long totalSchoolAdmins = userRepository.countByRole(UserRole.SchoolAdmin);
        long activeUsers = userRepository.countByActive(true);
        long inactiveUsers = userRepository.countByActive(false);

        Map<UserRole, Long> usersByRole = new EnumMap<>(UserRole.class);
        for (UserRole role : UserRole.values()) {
            usersByRole.put(role, userRepository.countByRole(role));
        }

        Map<ClassGroup, Long> studentsByClassGroup = new EnumMap<>(ClassGroup.class);
        for (ClassGroup classGroup : ClassGroup.values()) {
            studentsByClassGroup.put(classGroup, studentRepository.countByClassGroup(classGroup));
        }

        Map<Gender, Long> studentsByGender = new EnumMap<>(Gender.class);
        for (Gender gender : Gender.values()) {
            studentsByGender.put(gender, studentRepository.countByGender(gender));
        }

        Map<StudentStatus, Long> studentsByStatus = new EnumMap<>(StudentStatus.class);
        for (StudentStatus status : StudentStatus.values()) {
            studentsByStatus.put(status, studentRepository.countByStatus(status));
        }

        // Health Incident Management
        long totalHealthIncidents = healthIncidentRepository.count();
        long recentIncidents = healthIncidentRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long criticalIncidents = healthIncidentRepository.countByCreatedAtAfter(sevenDaysAgo);

        Map<HealthIncidentType, Long> incidentsByType = new EnumMap<>(HealthIncidentType.class);
        for (HealthIncidentType type : HealthIncidentType.values()) {
            incidentsByType.put(type, healthIncidentRepository.countByIncidentType(type));
        }

        // Medical Supply Management
        long totalMedicalSupplies = medicalSupplyRepository.count();
        long lowStockSupplies = medicalSupplyRepository.countByCurrentStockLessThanEqual(5);

        Map<MedicalSupplyStatus, Long> suppliesByStatus = new EnumMap<>(MedicalSupplyStatus.class);
        for (MedicalSupplyStatus status : MedicalSupplyStatus.values()) {
            suppliesByStatus.put(status, medicalSupplyRepository.countByStatus(status));
        }

        // Vaccination Campaign Status
        long totalVaccinationCampaigns = vaccinationCampaignRepository.count();

        Map<VaccinationCampaignStatus, Long> campaignsByStatus = new EnumMap<>(VaccinationCampaignStatus.class);
        for (VaccinationCampaignStatus status : VaccinationCampaignStatus.values()) {
            campaignsByStatus.put(status, vaccinationCampaignRepository.countByStatus(status));
        }

        long totalVaccinationConsents = vaccinationConsentRepository.count();

        Map<ConsentStatus, Long> consentsByStatus = new EnumMap<>(ConsentStatus.class);
        for (ConsentStatus status : ConsentStatus.values()) {
            consentsByStatus.put(status, vaccinationConsentRepository.countByStatus(status));
        }

        long totalSchoolVaccinations = schoolVaccinationRepository.count();

        Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus = new EnumMap<>(SchoolVaccinationStatus.class);
        for (SchoolVaccinationStatus status : SchoolVaccinationStatus.values()) {
            schoolVaccinationsByStatus.put(status, schoolVaccinationRepository.countByStatus(status));
        }

        // Student Health Records
        long totalChronicDiseaseRecords = studentChronicDiseaseRepository.count();

        Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus = new EnumMap<>(StudentChronicDiseaseStatus.class);
        for (StudentChronicDiseaseStatus status : StudentChronicDiseaseStatus.values()) {
            chronicDiseasesByStatus.put(status, studentChronicDiseaseRepository.countByStatus(status));
        }

        long totalStudentVaccinations = studentVaccinationRepository.count();

        Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus = new EnumMap<>(StudentVaccinationStatus.class);
        for (StudentVaccinationStatus status : StudentVaccinationStatus.values()) {
            studentVaccinationsByStatus.put(status, studentVaccinationRepository.countByStatus(status));
        }

        // Student Medication Management
        long totalStudentMedications = studentMedicationRepository.count();

        Map<MedicationStatus, Long> medicationsByStatus = new EnumMap<>(MedicationStatus.class);
        for (MedicationStatus status : MedicationStatus.values()) {
            medicationsByStatus.put(status, studentMedicationRepository.countByStatus(status));
        }

        long totalScheduledMedicationTasks = scheduledMedicationTaskRepository.count();

        Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus = new EnumMap<>(ScheduledMedicationTaskStatus.class);
        for (ScheduledMedicationTaskStatus status : ScheduledMedicationTaskStatus.values()) {
            scheduledTasksByStatus.put(status, scheduledMedicationTaskRepository.countByStatus(status));
        }

        long totalMedicationTransactions = studentMedicationTransactionRepository.count();

        Map<StudentMedicationTransactionType, Long> medicationTransactionsByType = new EnumMap<>(StudentMedicationTransactionType.class);
        for (StudentMedicationTransactionType type : StudentMedicationTransactionType.values()) {
            medicationTransactionsByType.put(type, studentMedicationTransactionRepository.countByTransactionType(type));
        }

        // Content Management
        long totalBlogs = blogRepository.count();

        Map<BlogStatus, Long> blogsByStatus = new EnumMap<>(BlogStatus.class);
        for (BlogStatus status : BlogStatus.values()) {
            blogsByStatus.put(status, blogRepository.countByStatus(status));
        }

        long recentBlogs = blogRepository.countByCreatedAtAfter(thirtyDaysAgo);

        Map<BlogCategory, Long> blogsByCategory = new EnumMap<>(BlogCategory.class);
        for (BlogCategory category : BlogCategory.values()) {
            blogsByCategory.put(category, blogRepository.countByCategory(category));
        }

        // Supply Transaction Management
        long totalSupplyTransactions = supplyTransactionRepository.count();

        Map<SupplyTransactionType, Long> supplyTransactionsByType = new EnumMap<>(SupplyTransactionType.class);
        for (SupplyTransactionType type : SupplyTransactionType.values()) {
            supplyTransactionsByType.put(type, supplyTransactionRepository.countBySupplyTransactionType(type));
        }

        // System Activity
        long totalNotifications = notificationRepository.count();
        long unreadNotifications = notificationRepository.countByReadFalse();
        long totalParentStudentLinks = parentStudentLinkRepository.count();

        Map<LinkStatus, Long> parentStudentLinksByStatus = new EnumMap<>(LinkStatus.class);
        for (LinkStatus status : LinkStatus.values()) {
            parentStudentLinksByStatus.put(status, parentStudentLinkRepository.countByStatus(status));
        }

        Map<RelationshipType, Long> parentStudentLinksByRelationship = new EnumMap<>(RelationshipType.class);
        for (RelationshipType type : RelationshipType.values()) {
            parentStudentLinksByRelationship.put(type, parentStudentLinkRepository.countByRelationshipType(type));
        }

        // Post Vaccination Monitoring
        long totalPostVaccinationMonitoring = postVaccinationMonitoringRepository.count();
        long monitoringWithSideEffects = postVaccinationMonitoringRepository.countByHasSideEffectsTrue();

        // Time Slot Management
        long totalMedicationTimeSlots = medicationTimeSlotRepository.count();

        Map<SchoolSession, Long> medicationTimeSlotsBySession = new EnumMap<>(SchoolSession.class);
        for (SchoolSession session : SchoolSession.values()) {
            medicationTimeSlotsBySession.put(session, medicationTimeSlotRepository.countBySchoolSessionHint(session));
        }

        return new DashboardAdminDto(
            totalStudents,
            totalParents,
            totalMedicalStaff,
            totalStaffManagers,
            totalSchoolAdmins,
            activeUsers,
            inactiveUsers,
            usersByRole,
            studentsByClassGroup,
            studentsByGender,
            studentsByStatus,
            totalHealthIncidents,
            recentIncidents,
            criticalIncidents,
            incidentsByType,
            totalMedicalSupplies,
            lowStockSupplies,
            suppliesByStatus,
            totalVaccinationCampaigns,
            campaignsByStatus,
            totalVaccinationConsents,
            consentsByStatus,
            totalSchoolVaccinations,
            schoolVaccinationsByStatus,
            totalChronicDiseaseRecords,
            chronicDiseasesByStatus,
            totalStudentVaccinations,
            studentVaccinationsByStatus,
            totalStudentMedications,
            medicationsByStatus,
            totalScheduledMedicationTasks,
            scheduledTasksByStatus,
            totalMedicationTransactions,
            medicationTransactionsByType,
            totalBlogs,
            recentBlogs,
            blogsByStatus,
            blogsByCategory,
            totalSupplyTransactions,
            supplyTransactionsByType,
            totalNotifications,
            unreadNotifications,
            totalParentStudentLinks,
            parentStudentLinksByStatus,
            parentStudentLinksByRelationship,
            totalPostVaccinationMonitoring,
            monitoringWithSideEffects,
            totalMedicationTimeSlots,
            medicationTimeSlotsBySession
        );
    }
}
