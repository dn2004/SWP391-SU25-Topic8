package com.fu.swp391.schoolhealthmanagementsystem.dto;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;

import java.util.Map;

public record DashboardStaffManagerDto(
    // User & Staff Management Statistics
    long totalUsers,
    long totalMedicalStaff,
    long totalParents,
    long totalSchoolAdmins,
    long activeUsers,
    long inactiveUsers,
    Map<UserRole, Long> usersByRole,

    // Student Statistics (for staff oversight)
    long totalStudents,
    Map<ClassGroup, Long> studentsByClassGroup,
    Map<Class, Long> studentsByClass,
    Map<StudentStatus, Long> studentsByStatus,

    // Daily Operations - Health Incidents
    long totalHealthIncidents,
    long recentHealthIncidents, // Last 7 days
    long todayHealthIncidents,
    Map<HealthIncidentType, Long> incidentsByType,

    // Medical Supply Management
    long totalMedicalSupplies,
    long lowStockSupplies,
    long expiredSupplies,
    Map<MedicalSupplyStatus, Long> suppliesByStatus,
    long totalSupplyTransactions,
    long todaySupplyTransactions,
    Map<SupplyTransactionType, Long> supplyTransactionsByType,

    // Medication Management
    long totalStudentMedications,
    long activeMedications,
    Map<MedicationStatus, Long> medicationsByStatus,
    long totalScheduledMedicationTasks,
    long todayScheduledTasks,
    long pendingMedicationTasks,
    long completedMedicationTasks,
    Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus,

    // Vaccination Management
    long totalVaccinationCampaigns,
    long activeVaccinationCampaigns,
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,
    long totalVaccinationConsents,
    long pendingConsents,
    Map<ConsentStatus, Long> consentsByStatus,
    long totalSchoolVaccinations,
    Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus,

    // Health Records Management
    long totalChronicDiseaseRecords,
    long pendingChronicDiseaseRecords,
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,
    long totalStudentVaccinations,
    long pendingStudentVaccinations,
    Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus,

    // Parent-Student Links Management
    long totalParentStudentLinks,
    long activeParentStudentLinks,
    Map<LinkStatus, Long> parentStudentLinksByStatus,
    Map<RelationshipType, Long> parentStudentLinksByRelationship,

    // Content Management
    long totalBlogs,
    long recentBlogs, // Last 30 days
    Map<BlogStatus, Long> blogsByStatus,
    Map<BlogCategory, Long> blogsByCategory,

    // System Activity & Monitoring
    long totalNotifications,
    long unreadNotifications,
    long totalPostVaccinationMonitoring,
    long monitoringWithSideEffects,

    // Time Management
    long totalMedicationTimeSlots,
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {
}
