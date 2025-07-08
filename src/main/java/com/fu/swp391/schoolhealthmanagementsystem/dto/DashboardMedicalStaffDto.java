package com.fu.swp391.schoolhealthmanagementsystem.dto;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;

import java.util.Map;

public record DashboardMedicalStaffDto(
    // Daily Care Overview
    long totalStudentsUnderCare,
    Map<ClassGroup, Long> studentsByClassGroup,
    Map<Class, Long> studentsByClass,

    // Today's Tasks & Schedules
    long todayScheduledMedicationTasks,
    long pendingMedicationTasks,
    long completedMedicationTasksToday,
    Map<ScheduledMedicationTaskStatus, Long> medicationTasksByStatus,
    long todayVaccinationSchedules,
    long completedVaccinationsToday,

    // Recent Health Incidents (Last 7 days)
    long recentHealthIncidents,
    long todayHealthIncidents,
    Map<HealthIncidentType, Long> recentIncidentsByType,

    // Student Medications Management
    long totalActiveMedications,
    long medicationsNeedingAttention, // Low doses, expiring soon
    long expiredMedications,
    Map<MedicationStatus, Long> medicationsByStatus,

    // Medical Supply Status
    long totalMedicalSupplies,
    long lowStockSupplies,
    long expiredSupplies,
    long criticalSupplies, // Extremely low stock
    Map<MedicalSupplyStatus, Long> suppliesByStatus,

    // Student Health Records
    long totalChronicDiseaseStudents,
    long chronicDiseaseNeedingMonitoring,
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,

    // Vaccination Management
    long upcomingVaccinationCampaigns,
    long activeVaccinationCampaigns,
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,
    long pendingVaccinationConsents,
    Map<ConsentStatus, Long> consentsByStatus,

    // Post-Vaccination Monitoring
    long totalPostVaccinationMonitoring,
    long monitoringWithSideEffects,
    long todayMonitoringDue,

    // Workload Distribution
    long totalNotifications,
    long unreadNotifications,
    long tasksAssignedToMe,
    long tasksCompletedByMeToday,

    // Time Slot Management
    long morningMedicationSlots,
    long afternoonMedicationSlots,
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {
}
