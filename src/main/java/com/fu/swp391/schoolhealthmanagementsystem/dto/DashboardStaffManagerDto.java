package com.fu.swp391.schoolhealthmanagementsystem.dto;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.Class;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Thống kê tổng quan cho Quản lý nhân viên")
public record DashboardStaffManagerDto(
    // Thống kê người dùng & nhân viên
    @Schema(description = "Tổng số người dùng")
    long totalUsers,
    @Schema(description = "Tổng số nhân viên y tế")
    long totalMedicalStaff,
    @Schema(description = "Tổng số phụ huynh")
    long totalParents,
    @Schema(description = "Tổng số quản trị viên trường")
    long totalSchoolAdmins,
    @Schema(description = "Số người dùng đang hoạt động")
    long activeUsers,
    @Schema(description = "Số người dùng không hoạt động")
    long inactiveUsers,
    @Schema(description = "Số người dùng theo vai trò")
    Map<UserRole, Long> usersByRole,

    // Thống kê học sinh (cho quản lý nhân viên)
    @Schema(description = "Tổng số học sinh")
    long totalStudents,
    @Schema(description = "Số học sinh theo nhóm lớp")
    Map<ClassGroup, Long> studentsByClassGroup,
    @Schema(description = "Số học sinh theo lớp")
    Map<Class, Long> studentsByClass,
    @Schema(description = "Số học sinh theo trạng thái")
    Map<StudentStatus, Long> studentsByStatus,

    // Hoạt động hàng ngày - Sự cố sức khỏe
    @Schema(description = "Tổng số sự cố sức khỏe")
    long totalHealthIncidents,
    @Schema(description = "Số sự cố sức khỏe gần đây")
    long recentHealthIncidents,
    @Schema(description = "Số sự cố sức khỏe hôm nay")
    long todayHealthIncidents,
    @Schema(description = "Số sự cố theo loại")
    Map<HealthIncidentType, Long> incidentsByType,

    // Quản lý vật tư y tế
    @Schema(description = "Tổng số vật tư y tế")
    long totalMedicalSupplies,
    @Schema(description = "Số vật tư sắp hết kho")
    long lowStockSupplies,
    @Schema(description = "Số vật tư đã hết hạn")
    long expiredSupplies,
    @Schema(description = "Số vật tư theo trạng thái")
    Map<MedicalSupplyStatus, Long> suppliesByStatus,
    @Schema(description = "Tổng số giao dịch vật tư")
    long totalSupplyTransactions,
    @Schema(description = "Số giao dịch vật tư hôm nay")
    long todaySupplyTransactions,
    @Schema(description = "Số giao dịch vật tư theo loại")
    Map<SupplyTransactionType, Long> supplyTransactionsByType,

    // Quản lý đơn thuốc
    @Schema(description = "Tổng số đơn thuốc của học sinh")
    long totalStudentMedications,
    @Schema(description = "Số đơn thuốc đang hoạt động")
    long activeMedications,
    @Schema(description = "Số đơn thuốc theo trạng thái")
    Map<MedicationStatus, Long> medicationsByStatus,
    @Schema(description = "Tổng số lịch uống thuốc đã lên lịch")
    long totalScheduledMedicationTasks,
    @Schema(description = "Số lịch uống thuốc hôm nay")
    long todayScheduledTasks,
    @Schema(description = "Số lịch uống thuốc đang chờ xử lý")
    long pendingMedicationTasks,
    @Schema(description = "Số lịch uống thuốc đã hoàn thành")
    long completedMedicationTasks,
    @Schema(description = "Số lịch uống thuốc theo trạng thái")
    Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus,

    // Quản lý tiêm chủng
    @Schema(description = "Tổng số chiến dịch tiêm chủng")
    long totalVaccinationCampaigns,
    @Schema(description = "Số chiến dịch tiêm chủng đang hoạt động")
    long activeVaccinationCampaigns,
    @Schema(description = "Số chiến dịch tiêm chủng theo trạng thái")
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,
    @Schema(description = "Tổng số phiếu đồng ý tiêm chủng")
    long totalVaccinationConsents,
    @Schema(description = "Số phiếu đồng ý đang chờ xử lý")
    long pendingConsents,
    @Schema(description = "Số phiếu đồng ý theo trạng thái")
    Map<ConsentStatus, Long> consentsByStatus,
    @Schema(description = "Tổng số lượt tiêm chủng tại trường")
    long totalSchoolVaccinations,
    @Schema(description = "Số lượt tiêm chủng tại trường theo trạng thái")
    Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus,

    // Quản lý hồ sơ sức khỏe
    @Schema(description = "Tổng số hồ sơ bệnh mãn tính")
    long totalChronicDiseaseRecords,
    @Schema(description = "Số hồ sơ bệnh mãn tính đang chờ xử lý")
    long pendingChronicDiseaseRecords,
    @Schema(description = "Số hồ sơ bệnh mãn tính theo trạng thái")
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,
    @Schema(description = "Tổng số lượt tiêm chủng của học sinh")
    long totalStudentVaccinations,
    @Schema(description = "Số lượt tiêm chủng của học sinh đang chờ xử lý")
    long pendingStudentVaccinations,
    @Schema(description = "Số lượt tiêm chủng của học sinh theo trạng thái")
    Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus,

    // Quản lý liên kết phụ huynh-học sinh
    @Schema(description = "Tổng số liên kết phụ huynh-học sinh")
    long totalParentStudentLinks,
    @Schema(description = "Số liên kết phụ huynh-học sinh đang hoạt động")
    long activeParentStudentLinks,
    @Schema(description = "Số liên kết phụ huynh-học sinh theo trạng thái")
    Map<LinkStatus, Long> parentStudentLinksByStatus,
    @Schema(description = "Số liên kết phụ huynh-học sinh theo mối quan hệ")
    Map<RelationshipType, Long> parentStudentLinksByRelationship,

    // Quản lý bài viết
    @Schema(description = "Tổng số bài viết")
    long totalBlogs,
    @Schema(description = "Số bài viết gần đây")
    long recentBlogs,
    @Schema(description = "Số bài viết theo trạng thái")
    Map<BlogStatus, Long> blogsByStatus,
    @Schema(description = "Số bài viết theo danh mục")
    Map<BlogCategory, Long> blogsByCategory,

    // Hoạt động hệ thống & giám sát
    @Schema(description = "Tổng số thông báo")
    long totalNotifications,
    @Schema(description = "Số thông báo chưa đọc")
    long unreadNotifications,
    @Schema(description = "Tổng số lượt theo dõi sau tiêm chủng")
    long totalPostVaccinationMonitoring,
    @Schema(description = "Số lượt theo dõi có tác dụng phụ")
    long monitoringWithSideEffects,

    // Quản lý thời gian
    @Schema(description = "Tổng số khung giờ uống thuốc")
    long totalMedicationTimeSlots,
    @Schema(description = "Số khung giờ uống thuốc theo buổi")
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {}
