package com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Thống kê tổng quan cho Quản trị viên")
public record DashboardAdminDto(
    @Schema(
        description = "Tổng số học sinh",
        example = "1200"
    )
    long totalStudents,

    @Schema(
        description = "Tổng số phụ huynh",
        example = "1100"
    )
    long totalParents,

    @Schema(
        description = "Tổng số nhân viên y tế",
        example = "10"
    )
    long totalMedicalStaff,

    @Schema(
        description = "Tổng số quản lý nhân viên",
        example = "3"
    )
    long totalStaffManagers,

    @Schema(
        description = "Tổng số quản trị viên trường",
        example = "2"
    )
    long totalSchoolAdmins,

    @Schema(
        description = "Số người dùng đang hoạt động",
        example = "1300"
    )
    long activeUsers,

    @Schema(
        description = "Số người dùng không hoạt động",
        example = "100"
    )
    long inactiveUsers,

    @Schema(
        description = "Số người dùng theo vai trò",
        example = """
        {
          "Nhân viên Y tế": 10,
          "Phụ huynh": 1100,
          "Quản lý Nhân sự/Nhân viên": 3
        }
        """
    )
    Map<UserRole, Long> usersByRole,

    // Thống kê học sinh (cho quản trị viên)
    @Schema(
        description = "Số học sinh theo nhóm lớp",
        example = """
        {
          "Mầm": 300,
          "Chồi": 400,
          "Lá": 500
        }
        """
    )
    Map<ClassGroup, Long> studentsByClassGroup,

    @Schema(
        description = "Số học sinh theo giới tính",
        example = """
        {
          "Nam": 600,
          "Nữ": 600
        }
        """
    )
    Map<Gender, Long> studentsByGender,

    @Schema(
        description = "Số học sinh theo trạng thái",
        example = """
        {
          "Hoạt Động": 1100,
          "Tốt Nghiệp": 100,
          "Thôi Học": 100
        }
        """
    )
    Map<StudentStatus, Long> studentsByStatus,

    // Quản lý sự cố sức khỏe
    @Schema(
        description = "Tổng số sự cố sức khỏe",
        example = "50"
    )
    long totalHealthIncidents,

    @Schema(
        description = "Số sự cố gần đây",
        example = "5"
    )
    long recentIncidents,

    @Schema(
        description = "Số sự cố nghiêm trọng",
        example = "1"
    )
    long criticalIncidents,

    @Schema(
        description = "Số sự cố theo loại",
        example = """
        {
          "Chấn thương nhẹ": 10,
          "Ốm đau": 5,
          "Phản ứng dị ứng": 5,
          "Chấn thương đầu": 5,
          "Sốt": 5,
          "Đau bụng": 8,
          "Khác": 12
        }
        """
    )
    Map<HealthIncidentType, Long> incidentsByType,

    // Quản lý vật tư y tế
    @Schema(
        description = "Tổng số vật tư y tế",
        example = "200"
    )
    long totalMedicalSupplies,

    @Schema(
        description = "Số vật tư sắp hết kho",
        example = "15"
    )
    long lowStockSupplies,

    @Schema(
        description = "Số vật tư theo trạng thái",
        example = """
        {
          "Sẵn có": 180,
          "Hết hàng": 20,
          "Hết hạn": 5,
          "Đã loại bỏ ra khỏi kho": 5
        }
        """
    )
    Map<MedicalSupplyStatus, Long> suppliesByStatus,

    // Chiến dịch tiêm chủng
    @Schema(
        description = "Tổng số chiến dịch tiêm chủng",
        example = "8"
    )
    long totalVaccinationCampaigns,

    @Schema(
        description = "Số chiến dịch theo trạng thái",
        example = """
        {
          "Nháp": 3,
          "Đã lên lịch": 5,
          "Đang chuẩn bị": 2,
          "Đang diễn ra": 1,
          "Đã hoàn thành": 2,
          "Đã hủy": 1
        }
        """
    )
    Map<VaccinationCampaignStatus, Long> campaignsByStatus,

    @Schema(
        description = "Tổng số phiếu đồng ý tiêm chủng",
        example = "900"
    )
    long totalVaccinationConsents,

    @Schema(
        description = "Số phiếu đồng ý theo trạng thái",
        example = """
        {
          "Đang chờ": 800,
          "Đồng ý": 100,
          "Từ chối": 100
        }
        """
    )
    Map<ConsentStatus, Long> consentsByStatus,

    @Schema(
        description = "Tổng số lượt tiêm chủng tại trường",
        example = "850"
    )
    long totalSchoolVaccinations,

    @Schema(
        description = "Số lượt tiêm chủng tại trường theo trạng thái",
        example = """
        {
          "Đã lên lịch": 800,
          "Đã hoàn thành": 50,
          "Vắng mặt": 30,
          "Từ chối": 20,
          "Đang theo dõi": 10
        }
        """
    )
    Map<SchoolVaccinationStatus, Long> schoolVaccinationsByStatus,

    // Hồ sơ sức khỏe học sinh
    @Schema(
        description = "Tổng số hồ sơ bệnh mãn tính",
        example = "30"
    )
    long totalChronicDiseaseRecords,

    @Schema(
        description = "Số hồ sơ bệnh mãn tính theo trạng thái",
        example = """
        {
          "Chờ xử lý": 20,
          "Chấp nhận": 10,
          "Từ chối": 0
        }
        """
    )
    Map<StudentChronicDiseaseStatus, Long> chronicDiseasesByStatus,

    @Schema(
        description = "Tổng số lượt tiêm chủng của học sinh",
        example = "1200"
    )
    long totalStudentVaccinations,

    @Schema(
        description = "Số lượt tiêm chủng của học sinh theo trạng thái",
        example = """
        {
          "Chờ xử lý": 1100,
          "Chấp nhận": 100,
          "Từ chối": 50
        }
        """
    )
    Map<StudentVaccinationStatus, Long> studentVaccinationsByStatus,

    // Quản lý đơn thuốc học sinh
    @Schema(
        description = "Tổng số đơn thuốc của học sinh",
        example = "400"
    )
    long totalStudentMedications,

    @Schema(
        description = "Số đơn thuốc theo trạng thái",
        example = """
        {
          "Sẵn có": 350,
          "Hết liều": 50,
          "Đã hết hạn": 10,
          "Đã trả lại phụ huynh": 5,
          "Bị thất lạc": 5,
          "Hủy bỏ": 5
        }
        """
    )
    Map<MedicationStatus, Long> medicationsByStatus,

    @Schema(
            description = "Tổng số lịch uống thuốc đã lên lịch", example = "1000")
    long totalScheduledMedicationTasks,

    @Schema(
        description = "Số lịch uống thuốc theo trạng thái",
        example = """
        {
          "Đã lên lịch": 900,
          "Đã cho uống": 100,
          "Bỏ qua - Vấn đề thuốc": 50,
          "Bỏ qua - Học sinh vắng": 30,
          "Bỏ qua - Học sinh từ chối": 20,
          "Đã cập nhật lịch trình khác": 10,
          "Bỏ qua - Quá hạn xử lý": 5,
          "Bỏ qua - Thuốc đã hủy": 5,
          "Không cho uống - Lý do khác": 10
        }
        """
    )
    Map<ScheduledMedicationTaskStatus, Long> scheduledTasksByStatus,

    @Schema(
        description = "Tổng số giao dịch thuốc",
        example = "200"
    )
    long totalMedicationTransactions,

    @Schema(
        description = "Số giao dịch thuốc phụ huynh gửi cho học sinh theo loại",
        example = """
        {
          "Nhập kho ban đầu": 120,
          "Đã cho uống": 80,
          "Thất lạc": 10,
          "Loại bỏ do hết hạn": 5,
          "Trả lại cho phụ huynh": 5,
          "Hủy thuốc": 10
        }
        """
    )
    Map<StudentMedicationTransactionType, Long> medicationTransactionsByType,

    // Quản lý bài viết
    @Schema(
        description = "Tổng số bài viết",
        example = "50"
    )
    long totalBlogs,

    @Schema(
        description = "Số bài viết gần đây",
        example = "5"
    )
    long recentBlogs,

    @Schema(
        description = "Số bài viết theo trạng thái",
        example = """
        {
          "Công khai": 40,
          "Riêng tư": 10
        }
        """
    )
    Map<BlogStatus, Long> blogsByStatus,

    @Schema(
        description = "Số bài viết theo danh mục",
        example = """
        {
          "Tin tức sức khỏe": 20,
          "Dinh dưỡng": 30,
          "Sức khỏe tâm thần": 30,
          "Phòng ngừa bệnh tật": 30,
          "Sơ cấp cứu": 40,
          "Hoạt động thể chất": 60,
          "Phát triển và tăng trưởng": 73,
          "Giáo dục sức khỏe": 30,
          "Khác": 20
        }
        """
    )
    Map<BlogCategory, Long> blogsByCategory,

    // Quản lý giao dịch vật tư
    @Schema(
        description = "Tổng số giao dịch vật tư",
        example = "100"
    )
    long totalSupplyTransactions,

    @Schema(
        description = "Số giao dịch vật tư theo loại",
        example = """
        {
          "Nhập kho mới": 50,
          "Sử dụng cho sự cố": 30,
          "Điều chỉnh giảm": 20,
          "Điều chỉnh tăng": 10,
          "Trả lại từ sự cố": 10,
          "Loại bỏ hết vì hết hạn": 5
        }
        """
    )
    Map<SupplyTransactionType, Long> supplyTransactionsByType,

    // Hoạt động hệ thống
    @Schema(
        description = "Tổng số thông báo",
        example = "300"
    )
    long totalNotifications,

    @Schema(
        description = "Số thông báo chưa đọc",
        example = "20"
    )
    long unreadNotifications,

    @Schema(
        description = "Tổng số liên kết phụ huynh-học sinh",
        example = "1100"
    )
    long totalParentStudentLinks,

    @Schema(
        description = "Số liên kết phụ huynh-học sinh theo trạng thái",
        example = """
        {
          "Hoạt động": 1000,
          "Không hoạt động": 100
        }
        """
    )
    Map<LinkStatus, Long> parentStudentLinksByStatus,

    @Schema(
        description = "Số liên kết phụ huynh-học sinh theo mối quan hệ",
        example = """
        {
          "Cha": 600,
          "Mẹ": 500,
          "Người giám hộ": 200,
          "Ông": 50,
          "Bà": 50,
          "Khác": 100
        }
        """
    )
    Map<RelationshipType, Long> parentStudentLinksByRelationship,

    // Theo dõi sau tiêm chủng
    @Schema(
        description = "Tổng số lượt theo dõi sau tiêm chủng",
        example = "700"
    )
    long totalPostVaccinationMonitoring,

    @Schema(
        description = "Số lượt theo dõi có tác dụng phụ",
        example = "12"
    )
    long monitoringWithSideEffects,

    // Quản lý khung giờ uống thuốc
    @Schema(
        description = "Tổng số khung giờ uống thuốc",
        example = "8"
    )
    long totalMedicationTimeSlots,

    @Schema(
        description = "Số khung giờ uống thuốc theo buổi",
        example = """
        {
          "Sáng": 4,
          "Chiều": 4
        }
        """
    )
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {}
