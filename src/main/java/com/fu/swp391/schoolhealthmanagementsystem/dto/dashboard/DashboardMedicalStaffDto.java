package com.fu.swp391.schoolhealthmanagementsystem.dto.dashboard;

import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Thống kê tổng quan cho Nhân viên y tế")
public record DashboardMedicalStaffDto(
    @Schema(
        description = "Tổng số học sinh được chăm sóc",
        example = "800"
    )
    long totalStudentsUnderCare,

    @Schema(
        description = "Số học sinh theo nhóm lớp",
        example = """
        {
          "Mầm": 200,
          "Chồi": 300,
          "Lá": 300
        }
        """
    )
    Map<ClassGroup, Long> studentsByClassGroup,

    @Schema(
        description = "Tổng số lịch uống thuốc hôm nay",
        example = "120"
    )
    long todayScheduledMedicationTasks,

    @Schema(
        description = "Số lịch uống thuốc đang chờ xử lý",
        example = "10"
    )
    long pendingMedicationTasks,

    @Schema(
        description = "Số lịch uống thuốc đã hoàn thành hôm nay",
        example = "100"
    )
    long completedMedicationTasksToday,

    @Schema(
        description = "Số lịch uống thuốc theo trạng thái",
        example = """
        {
          "Chờ xử lý": 10,
          "Đã hoàn thành": 100,
          "Đã bỏ qua": 10
        }
        """
    )
    Map<ScheduledMedicationTaskStatus, Long> medicationTasksByStatus,

    @Schema(
        description = "Tổng số lịch tiêm chủng hôm nay",
        example = "15"
    )
    long todayVaccinationSchedules,

    @Schema(
        description = "Số lượt tiêm chủng đã hoàn thành hôm nay",
        example = "12"
    )
    long completedVaccinationsToday,

    @Schema(
        description = "Số sự cố sức khỏe gần đây",
        example = "7"
    )
    long recentHealthIncidents,

    @Schema(
        description = "Số sự cố sức khỏe hôm nay",
        example = "2"
    )
    long todayHealthIncidents,

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
    Map<HealthIncidentType, Long> recentIncidentsByType,

    @Schema(
        description = "Tổng số đơn thuốc đang hoạt động",
        example = "300"
    )
    long totalActiveMedications,

    @Schema(
        description = "Số đơn thuốc cần chú ý (gần hết, sắp hết hạn)",
        example = "5"
    )
    long medicationsNeedingAttention,

    @Schema(
        description = "Số đơn thuốc đã hết hạn",
        example = "2"
    )
    long expiredMedications,

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
        description = "Tổng số vật tư y tế",
        example = "150"
    )
    long totalMedicalSupplies,

    @Schema(
        description = "Số vật tư sắp hết kho",
        example = "8"
    )
    long lowStockSupplies,

    @Schema(
        description = "Số vật tư đã hết hạn",
        example = "1"
    )
    long expiredSupplies,

    @Schema(
        description = "Số vật tư cực kỳ thiếu",
        example = "2"
    )
    long criticalSupplies,

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

    @Schema(
        description = "Tổng số học sinh có bệnh mãn tính",
        example = "40"
    )
    long totalChronicDiseaseStudents,

    @Schema(
        description = "Số học sinh cần theo dõi bệnh mãn tính",
        example = "5"
    )
    long chronicDiseaseNeedingMonitoring,

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
        description = "Số chiến dịch tiêm chủng sắp tới",
        example = "3"
    )
    long upcomingVaccinationCampaigns,

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
        description = "Số phiếu đồng ý tiêm chủng đang chờ xử lý",
        example = "20"
    )
    long pendingVaccinationConsents,

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
        description = "Tổng số lượt theo dõi sau tiêm chủng",
        example = "60"
    )
    long totalPostVaccinationMonitoring,

    @Schema(
        description = "Số lượt theo dõi có tác dụng phụ",
        example = "3"
    )
    long monitoringWithSideEffects,

    @Schema(
        description = "Số lượt theo dõi cần thực hiện hôm nay",
        example = "5"
    )
    long todayMonitoringDue,

    @Schema(
        description = "Tổng số thông báo",
        example = "80"
    )
    long totalNotifications,

    @Schema(
        description = "Số thông báo chưa đọc",
        example = "6"
    )
    long unreadNotifications,

    @Schema(
        description = "Số nhiệm vụ được giao cho tôi",
        example = "4"
    )
    long tasksAssignedToMe,

    @Schema(
        description = "Số nhiệm vụ tôi đã hoàn thành hôm nay",
        example = "2"
    )
    long tasksCompletedByMeToday,

    @Schema(
        description = "Số khung giờ uống thuốc buổi sáng",
        example = "3"
    )
    long morningMedicationSlots,

    @Schema(
        description = "Số khung giờ uống thuốc buổi chiều",
        example = "2"
    )
    long afternoonMedicationSlots,

    @Schema(
        description = "Số khung giờ uống thuốc theo buổi",
        example = """
        {
          "Sáng": 3,
          "Chiều": 2
        }
        """
    )
    Map<SchoolSession, Long> medicationTimeSlotsBySession
) {}
