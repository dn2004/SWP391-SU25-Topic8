package com.fu.swp391.schoolhealthmanagementsystem.dto;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;

import java.util.Map;

public record DashboardAdminDto(
    // User & Student Statistics
    long totalStudents,
    long totalParents,
    long totalMedicalStaff,
    long totalStaffManagers,
    long totalSchoolAdmins,
    long activeUsers,
    long inactiveUsers,
    Map<ClassGroup, Long> studentsByClassGroup,
    Map<Class, Long> studentsByClass,
    Map<Gender, Long> studentsByGender,
    Map<StudentStatus, Long> studentsByStatus,

    // Health Incident Management
    long totalHealthIncidents,
    long recentIncidents,
    long criticalIncidents,
    Map<HealthIncidentType, Long> incidentsByType,

    // Medical Supply Management
    long totalMedicalSupplies,
    long lowStockSupplies,
    Map<MedicalSupplyStatus, Long> suppliesByStatus,

    // Vaccination Campaign Status
    long totalVaccinationCampaigns,
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,
    long totalVaccinationConsents,
    Map<ConsentStatus, Long> consentsByStatus,
    long totalSchoolVaccinations,
    Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus,

    // Student Health Records
    long totalChronicDiseaseRecords,
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,
    long totalStudentVaccinations,
    Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus,

    // Student Medication Management
    long totalStudentMedications,
    Map<MedicationStatus, Long> medicationsByStatus,
    long totalScheduledMedicationTasks,
    Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus,
    long totalMedicationTransactions,
    Map<StudentMedicationTransactionType, Long> medicationTransactionsByType,

    // Content Management
    long totalBlogs,
    Map<BlogStatus, Long> blogsByStatus,
    long recentBlogs,
    Map<BlogCategory, Long> blogsByCategory,

    // Supply Transaction Management
    long totalSupplyTransactions,
    Map<SupplyTransactionType, Long> supplyTransactionsByType,

    // System Activity
    long totalNotifications,
    long unreadNotifications,
    long totalParentStudentLinks,
    Map<LinkStatus, Long> parentStudentLinksByStatus,
    Map<RelationshipType, Long> parentStudentLinksByRelationship,

    // Post Vaccination Monitoring
    long totalPostVaccinationMonitoring,
    long monitoringWithSideEffects,

    // Time Slot Management
    long totalMedicationTimeSlots,
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {
}
