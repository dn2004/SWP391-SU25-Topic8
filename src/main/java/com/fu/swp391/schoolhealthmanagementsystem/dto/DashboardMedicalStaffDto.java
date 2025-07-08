package com.fu.swp391.schoolhealthmanagementsystem.dto;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Thống kê tổng quan cho Nhân viên y tế")
public record DashboardMedicalStaffDto(
    // Tổng quan chăm sóc hàng ngày
    @Schema(description = "Tổng số học sinh được chăm sóc")
    long totalStudentsUnderCare,
    @Schema(description = "Số học sinh theo nhóm lớp")
    Map<ClassGroup, Long> studentsByClassGroup,
    @Schema(description = "Số học sinh theo lớp")
    Map<Class, Long> studentsByClass,

    // Nhiệm vụ & lịch trình hôm nay
    @Schema(description = "Tổng số lịch uống thuốc hôm nay")
    long todayScheduledMedicationTasks,
    @Schema(description = "Số lịch uống thuốc đang chờ xử lý")
    long pendingMedicationTasks,
    @Schema(description = "Số lịch uống thuốc đã hoàn thành hôm nay")
    long completedMedicationTasksToday,
    @Schema(description = "Số lịch uống thuốc theo trạng thái")
    Map<ScheduledMedicationTaskStatus, Long> medicationTasksByStatus,
    @Schema(description = "Tổng số lịch tiêm chủng hôm nay")
    long todayVaccinationSchedules,
    @Schema(description = "Số lượt tiêm chủng đã hoàn thành hôm nay")
    long completedVaccinationsToday,

    // Sự cố sức khỏe gần đây (7 ngày)
    @Schema(description = "Số sự cố sức khỏe gần đây")
    long recentHealthIncidents,
    @Schema(description = "Số sự cố sức khỏe hôm nay")
    long todayHealthIncidents,
    @Schema(description = "Số sự cố gần đây theo loại")
    Map<HealthIncidentType, Long> recentIncidentsByType,

    // Quản lý đơn thuốc học sinh
    @Schema(description = "Tổng số đơn thuốc đang hoạt động")
    long totalActiveMedications,
    @Schema(description = "Số đơn thuốc cần chú ý (gần hết, sắp hết hạn)")
    long medicationsNeedingAttention,
    @Schema(description = "Số đơn thuốc đã hết hạn")
    long expiredMedications,
    @Schema(description = "Số đơn thuốc theo trạng thái")
    Map<MedicationStatus, Long> medicationsByStatus,

    // Quản lý vật tư y tế
    @Schema(description = "Tổng số vật tư y tế")
    long totalMedicalSupplies,
    @Schema(description = "Số vật tư sắp hết kho")
    long lowStockSupplies,
    @Schema(description = "Số vật tư đã hết hạn")
    long expiredSupplies,
    @Schema(description = "Số vật tư cực kỳ thiếu")
    long criticalSupplies,
    @Schema(description = "Số vật tư theo trạng thái")
    Map<MedicalSupplyStatus, Long> suppliesByStatus,

    // Hồ sơ sức khỏe học sinh
    @Schema(description = "Tổng số học sinh có bệnh mãn tính")
    long totalChronicDiseaseStudents,
    @Schema(description = "Số học sinh cần theo dõi bệnh mãn tính")
    long chronicDiseaseNeedingMonitoring,
    @Schema(description = "Số hồ sơ bệnh mãn tính theo trạng thái")
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,

    // Quản lý tiêm chủng
    @Schema(description = "Số chiến dịch tiêm chủng sắp tới")
    long upcomingVaccinationCampaigns,
    @Schema(description = "Số chiến dịch tiêm chủng đang hoạt động")
    long activeVaccinationCampaigns,
    @Schema(description = "Số chiến dịch tiêm chủng theo trạng thái")
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,
    @Schema(description = "Số phiếu đồng ý tiêm chủng đang chờ xử lý")
    long pendingVaccinationConsents,
    @Schema(description = "Số phiếu đồng ý tiêm chủng theo trạng thái")
    Map<ConsentStatus, Long> consentsByStatus,

    // Theo dõi sau tiêm chủng
    @Schema(description = "Tổng số lượt theo dõi sau tiêm chủng")
    long totalPostVaccinationMonitoring,
    @Schema(description = "Số lượt theo dõi có tác dụng phụ")
    long monitoringWithSideEffects,
    @Schema(description = "Số lượt theo dõi cần thực hiện hôm nay")
    long todayMonitoringDue,

    // Phân bổ công việc
    @Schema(description = "Tổng số thông báo")
    long totalNotifications,
    @Schema(description = "Số thông báo chưa đọc")
    long unreadNotifications,
    @Schema(description = "Số nhiệm vụ được giao cho tôi")
    long tasksAssignedToMe,
    @Schema(description = "Số nhiệm vụ tôi đã hoàn thành hôm nay")
    long tasksCompletedByMeToday,

    // Quản lý khung giờ uống thuốc
    @Schema(description = "Số khung giờ uống thuốc buổi sáng")
    long morningMedicationSlots,
    @Schema(description = "Số khung giờ uống thuốc buổi chiều")
    long afternoonMedicationSlots,
    @Schema(description = "Số khung giờ uống thuốc theo bu��i")
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {}
