package com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Thống kê tổng quan cho Quản lý nhân viên")
public record DashboardStaffManagerDto(
    @Schema(
        description = "Tổng số người dùng",
        example = "1500"
    )
    long totalUsers,

    @Schema(
        description = "Tổng số nhân viên y tế",
        example = "12"
    )
    long totalMedicalStaff,

    @Schema(
        description = "Tổng số phụ huynh",
        example = "1200"
    )
    long totalParents,

    @Schema(
        description = "Tổng số quản trị viên trường",
        example = "3"
    )
    long totalSchoolAdmins,

    @Schema(
        description = "Số người dùng đang hoạt động",
        example = "1400"
    )
    long activeUsers,

    @Schema(
        description = "Số người dùng không hoạt động",
        example = "100"
    )
    long inactiveUsers,

    @Schema(
        description = "Số người dùng theo vai trò, ngoại trừ Admin/System",
        example = """
        {
          "Nhân viên Y tế": 12,
          "Phụ huynh": 1200,
          "Quản lý Nhân sự/Nhân viên": 3
        }
        """
    )
    Map<UserRole, Long> usersByRole,

    // Thống kê học sinh (cho quản lý nhân viên)
    @Schema(
        description = "Tổng số học sinh",
        example = "900"
    )
    long totalStudents,

    @Schema(
        description = "Số học sinh theo nhóm lớp",
        example = """
        {
          "Mầm": 300,
          "Chồi": 400,
          "Lá": 600
        }
        """
    )
    Map<ClassGroup, Long> studentsByClassGroup,

    @Schema(
        description = "Số học sinh theo trạng thái",
        example = """
        {
          "Hoạt Động": 850,
          "Tốt Nghiệp": 50,
          "Thôi Học": 30
        }
        """
    )
    Map<StudentStatus, Long> studentsByStatus,

    // Hoạt động hàng ngày - Sự cố sức khỏe
    @Schema(
        description = "Tổng số sự cố sức khỏe",
        example = "40"
    )
    long totalHealthIncidents,

    @Schema(
        description = "Số sự cố sức khỏe gần đây",
        example = "6"
    )
    long recentHealthIncidents,

    @Schema(
        description = "Số sự cố sức khỏe hôm nay",
        example = "1"
    )
    long todayHealthIncidents,

    @Schema(
        description = "Số sự cố theo loại",
        example = """
        {
          "Chấn thương": 20,
          "Sốt": 10,
          "Dị ứng": 5,
          "Đau bụng": 5
        }
        """
    )
    Map<HealthIncidentType, Long> incidentsByType,

    // Quản lý vật tư y tế
    @Schema(
        description = "Tổng số vật tư y tế",
        example = "180"
    )
    long totalMedicalSupplies,

    @Schema(
        description = "Số vật tư sắp hết kho",
        example = "10"
    )
    long lowStockSupplies,

    @Schema(
        description = "Số vật tư đã hết hạn",
        example = "2"
    )
    long expiredSupplies,

    @Schema(
        description = "Số vật tư theo trạng thái",
        example = """
        {
          "Trong kho": 160,
          "Sắp hết": 10,
          "Hết hàng": 8,
          "Hết hạn": 2
        }
        """
    )
    Map<MedicalSupplyStatus, Long> suppliesByStatus,

    @Schema(
        description = "Tổng số giao dịch vật tư",
        example = "60"
    )
    long totalSupplyTransactions,

    @Schema(
        description = "Số giao dịch vật tư hôm nay",
        example = "3"
    )
    long todaySupplyTransactions,

    @Schema(
        description = "Số giao dịch vật tư theo loại",
        example = """
        {
          "Nhập kho": 40,
          "Xuất kho": 20,
          "Điều chỉnh": 5
        }
        """
    )
    Map<SupplyTransactionType, Long> supplyTransactionsByType,

    // Quản lý đơn thuốc
    @Schema(
        description = "Tổng số đơn thuốc của học sinh",
        example = "350"
    )
    long totalStudentMedications,

    @Schema(
        description = "Số đơn thuốc đang hoạt động",
        example = "300"
    )
    long activeMedications,

    @Schema(
        description = "Số đơn thuốc phụ huynh gửi cho trường theo trạng thái",
        example = """
        {
          "Sẵn có": 300,
          "Hết liều": 50,
          "Đã hết hạn": 60,
          "Đã trả lại phụ huynh": 10,
          "Bị thất lạc": 5
        }
        """
    )
    Map<MedicationStatus, Long> medicationsByStatus,

    @Schema(
        description = "Tổng số lịch uống thuốc đã lên lịch",
        example = "900"
    )
    long totalScheduledMedicationTasks,

    @Schema(
        description = "Số lịch uống thuốc hôm nay",
        example = "40"
    )
    long todayScheduledTasks,

    @Schema(
        description = "Số lịch uống thuốc đang chờ xử lý",
        example = "5"
    )
    long pendingMedicationTasks,

    @Schema(
        description = "Số lịch uống thuốc đã hoàn thành",
        example = "35"
    )
    long completedMedicationTasks,

    @Schema(
        description = "Số lịch uống thuốc theo trạng thái",
        example = """
        {
          "Chờ xử lý": 5,
          "Đã hoàn thành": 35,
          "Đã bỏ qua": 10
        }
        """
    )
    Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus,

    // Quản lý tiêm chủng
    @Schema(
        description = "Tổng số chiến dịch tiêm chủng",
        example = "7"
    )
    long totalVaccinationCampaigns,

    @Schema(
        description = "Số chiến dịch tiêm chủng đang hoạt động",
        example = "2"
    )
    long activeVaccinationCampaigns,

    @Schema(
        description = "Số chiến dịch tiêm chủng theo trạng thái",
        example = """
        {
          "Nháp": 2,
          "Đã lên lịch": 5,
          "Đang chuẩn bị": 1,
          "Đang diễn ra": 2,
          "Đã hoàn thành": 1,
          "Đã hủy": 0
        }
        """
    )
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,

    @Schema(
        description = "Tổng số phiếu đồng ý tiêm chủng",
        example = "700"
    )
    long totalVaccinationConsents,

    @Schema(
        description = "Số phiếu đồng ý đang chờ xử lý",
        example = "15"
    )
    long pendingConsents,

    @Schema(
        description = "Số phiếu đồng ý theo trạng thái",
        example = """
        {
          "Chờ xử lý": 15,
          "Đã chấp thuận": 685,
          "Từ chối": 0
        }
        """
    )
    Map<ConsentStatus, Long> consentsByStatus,

    @Schema(
        description = "Tổng số lượt tiêm chủng tại trường",
        example = "600"
    )
    long totalSchoolVaccinations,

    @Schema(
        description = "Số lượt tiêm chủng tại trường theo trạng thái",
        example = """
        {
          "Đã lên lịch": 550,
          "Đã hoàn thành": 50,
          "Vắng mặt": 0,
          "Từ chối": 0,
          "Đang theo dõi": 0
        }
        """
    )
    Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus,

    // Quản lý hồ sơ sức khỏe
    @Schema(
        description = "Tổng số hồ sơ bệnh mãn tính",
        example = "25"
    )
    long totalChronicDiseaseRecords,

    @Schema(
        description = "Số hồ sơ bệnh mãn tính đang chờ xử lý",
        example = "2"
    )
    long pendingChronicDiseaseRecords,

    @Schema(
        description = "Số hồ sơ bệnh mãn tính theo trạng thái",
        example = """
        {
          "Chờ xử lý": 2,
          "Chấp nhận": 23,
          "Từ chối": 0
        }
        """
    )
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,

    @Schema(
        description = "Tổng số lượt tiêm chủng của học sinh",
        example = "800"
    )
    long totalStudentVaccinations,

    @Schema(
        description = "Số lượt tiêm chủng của học sinh đang chờ xử lý",
        example = "10"
    )
    long pendingStudentVaccinations,

    @Schema(
        description = "Số lượt tiêm chủng của học sinh theo trạng thái",
        example = """
        {
          "Chờ xử lý": 10,
          "Chấp nhận": 790,
          "Từ chối": 0
        }
        """
    )
    Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus,

    // Quản lý liên kết phụ huynh-học sinh
    @Schema(
        description = "Tổng số liên kết ph�� huynh-học sinh",
        example = "1000"
    )
    long totalParentStudentLinks,

    @Schema(
        description = "Số liên kết phụ huynh-học sinh đang hoạt động",
        example = "950"
    )
    long activeParentStudentLinks,

    @Schema(
        description = "Số liên kết phụ huynh-học sinh theo trạng thái",
        example = """
        {
          "Hoạt động": 950,
          "Không hoạt động": 50
        }
        """
    )
    Map<LinkStatus, Long> parentStudentLinksByStatus,

    @Schema(
        description = "Số liên kết phụ huynh-học sinh theo mối quan hệ",
        example = """
        {
          "Bố": 500,
          "Mẹ": 450,
          "Người giám hộ": 50,
          "Ông": 10,
          "Bà": 10,
          "Khác": 30
        }
        """
    )
    Map<RelationshipType, Long> parentStudentLinksByRelationship,

    // Quản lý bài viết
    @Schema(
        description = "Tổng số bài viết",
        example = "40"
    )
    long totalBlogs,
    @Schema(
        description = "Số bài viết gần đây",
        example = "4"
    )
    long recentBlogs,

    @Schema(
        description = "Số bài viết theo trạng thái",
        example = """
        {
          "Công khai": 35,
          "Riêng tư": 5
        }
        """
    )
    Map<BlogStatus, Long> blogsByStatus,

    @Schema(
        description = "Số bài viết theo danh mục",
        example = """
        {
          "Tin tức sức khỏe": 20,
          "Dinh dưỡng": 20,
          "Sức khỏe tâm thần": 10,
          "Phòng ngừa bệnh tật": 5,
          "Sơ cấp cứu": 5,
          "Hoạt động thể chất": 5,
          "Phát triển và tăng trưởng": 5,
          "Giáo dục sức khỏe": 5,
          "Khác": 5
        }
        """
    )
    Map<BlogCategory, Long> blogsByCategory,

    // Hoạt động hệ thống & giám sát
    @Schema(
        description = "Tổng số thông báo",
        example = "60"
    )
    long totalNotifications,

    @Schema(
        description = "Số thông báo chưa đọc",
        example = "5"
    )
    long unreadNotifications,

    @Schema(
        description = "Tổng số lượt theo dõi sau tiêm chủng",
        example = "50"
    )
    long totalPostVaccinationMonitoring,
    @Schema(
        description = "Số lượt theo dõi có tác dụng phụ",
        example = "1"
    )
    long monitoringWithSideEffects,

    // Quản lý thời gian
    @Schema(
        description = "Tổng số khung giờ uống thuốc",
        example = "6"
    )
    long totalMedicationTimeSlots,

    @Schema(
        description = "Số khung giờ uống thuốc theo buổi",
        example = """
        {
          "Sáng": 4,
          "Chiều": 2
        }
        """
    )
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {}
