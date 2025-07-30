package com.fu.swp391.schoolhealthmanagementsystem.scheduler;

import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.VaccinationCampaignRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.VaccinationConsentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VaccinationScheduler {

    private final VaccinationCampaignRepository campaignRepository;
    private final VaccinationConsentRepository consentRepository;
    private final NotificationService notificationService;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    /**
     * Tự động xử lý các phiếu đồng ý đã quá hạn.
     * - Tìm các chiến dịch đang ở trạng thái SCHEDULED mà đã qua hạn chót.
     * - Chuyển các phiếu đồng ý PENDING thành DECLINED.
     * - Chuyển trạng thái chiến dịch sang PREPARING.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void processOverdueConsents() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Bộ lập lịch: Đang xử lý các phiếu đồng ý quá hạn cho ngày hạn chót: {}", yesterday);

        List<VaccinationCampaign> overdueCampaigns = campaignRepository.findByStatusAndConsentDeadline(
                VaccinationCampaignStatus.SCHEDULED, yesterday);

        if (overdueCampaigns.isEmpty()) {
            log.info("Bộ lập lịch: Không có chiến dịch nào quá hạn.");
            return;
        }

        log.info("Bộ lập lịch: Tìm thấy {} chiến dịch quá hạn. Đang xử lý...", overdueCampaigns.size());
        for (VaccinationCampaign campaign : overdueCampaigns) {
            List<VaccinationConsent> pendingConsents = consentRepository.findByCampaignAndStatus(campaign, ConsentStatus.PENDING);

            if (!pendingConsents.isEmpty()) {
                log.info("Bộ lập lịch: Tìm thấy {} phiếu đồng ý đang chờ cho chiến dịch ID {}. Đang chuyển sang từ chối.",
                        pendingConsents.size(), campaign.getCampaignId());
                for (VaccinationConsent consent : pendingConsents) {
                    consent.setStatus(ConsentStatus.DECLINED);
                    consent.setResponseReceivedAt(LocalDateTime.now());
                }
                consentRepository.saveAll(pendingConsents);
            }

            log.info("Bộ lập lịch: Đang cập nhật trạng thái chiến dịch ID {} từ SCHEDULED sang PREPARING.", campaign.getCampaignId());
            campaign.setStatus(VaccinationCampaignStatus.PREPARING);
            campaignRepository.save(campaign);
        }
        log.info("Bộ lập lịch: Đã xử lý xong các phiếu đồng ý quá hạn.");
    }

    /**
     * Chạy vào 2 giờ sáng mỗi ngày.
     * - Tự động hoàn thành các chiến dịch đã qua ngày tiêm chủng.
     * - Đối với những tiêm chủng tại trường có trạng thái là đã lên lịch -> cập nhật trạng thái thành vắng mặt.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoCompleteCampaigns() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Bộ lập lịch: Đang tự động hoàn thành các chiến dịch cho ngày tiêm chủng: {}", yesterday);

        List<VaccinationCampaign> completedCampaigns = campaignRepository.findByStatusAndVaccinationDate(
                VaccinationCampaignStatus.IN_PROGRESS, yesterday);

        if (completedCampaigns.isEmpty()) {
            log.info("Bộ lập lịch: Không có chiến dịch nào cần tự động hoàn thành.");
            return;
        }

        log.info("Bộ lập lịch: Tìm thấy {} chiến dịch cần tự động hoàn thành. Đang xử lý...", completedCampaigns.size());
        for (VaccinationCampaign campaign : completedCampaigns) {
            log.info("Bộ lập lịch: Đang cập nhật trạng thái chiến dịch ID {} từ IN_PROGRESS sang COMPLETED.", campaign.getCampaignId());
            campaign.setStatus(VaccinationCampaignStatus.COMPLETED);

                List<SchoolVaccination> scheduledVaccination = campaign.getVaccinations().stream()
                        .filter(sv -> sv.getStatus() == SchoolVaccinationStatus.SCHEDULED)
                        .toList();
                scheduledVaccination.forEach(schoolVaccination -> {
                    log.info("Bộ lập lịch: Cập nhật trạng thái tiêm chủng ID {} từ SCHEDULED sang ABSENT.", schoolVaccination.getSchoolVaccinationId());
                    schoolVaccination.setStatus(SchoolVaccinationStatus.ABSENT);
                });
                campaignRepository.save(campaign);
        }

        log.info("Bộ lập lịch: Đã hoàn thành tự động cập nhật trạng thái các chiến dịch.");
    }

    /**
     * Chạy vào 9 giờ sáng mỗi ngày.
     * Gửi thông báo nhắc nhở cho các phụ huynh chưa phản hồi khi gần đến hạn chót.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void sendDeadlineReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Bộ lập lịch: Đang gửi nhắc nhở hạn chót xác nhận phiếu đồng ý cho ngày: {}", tomorrow);

        List<VaccinationCampaign> campaignsWithUpcomingDeadline = campaignRepository.findByStatusAndConsentDeadline(
                VaccinationCampaignStatus.SCHEDULED, tomorrow);

        if (campaignsWithUpcomingDeadline.isEmpty()) {
            log.info("Bộ lập lịch: Không có chiến dịch nào sắp đến hạn chót.");
            return;
        }

        log.info("Bộ lập lịch: Tìm thấy {} chiến dịch sắp đến hạn chót. Đang xử lý...", campaignsWithUpcomingDeadline.size());
        for (VaccinationCampaign campaign : campaignsWithUpcomingDeadline) {
            List<VaccinationConsent> consentsToRemind = consentRepository.findByCampaignAndStatus(campaign, ConsentStatus.PENDING);

            log.info("Bộ lập lịch: Đang gửi {} nhắc nhở cho chiến dịch ID {}.", consentsToRemind.size(), campaign.getCampaignId());
            for (VaccinationConsent consent : consentsToRemind) {
                // Lấy danh sách phụ huynh qua ParentStudentLink
                List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudent(consent.getStudent());
                parentLinks.forEach(parentLink -> {
                    User parent = parentLink.getParent();
                    if (parent != null) {
                        String content = String.format(
                                "Nhắc nhở: Hạn chót xác nhận phiếu đồng ý tiêm chủng '%s' cho học sinh %s là vào ngày mai (%s).",
                                campaign.getCampaignName(), consent.getStudent().getFullName(),
                                campaign.getConsentDeadline().toString());
                        String link = "/vaccination/consent/" + consent.getConsentId();
                        try {
                            notificationService.createAndSendNotification(
                                    parent.getEmail(), content, link, "hệ thống");
                            consent.setReminderSentAt(LocalDateTime.now());
                            consentRepository.save(consent);
                            log.info("Đã gửi nhắc nhở hạn chót xác nhận phiếu đồng ý cho phụ huynh ID: {} (email: {}) của học sinh: {}.",
                                    parent.getUserId(), parent.getEmail(), consent.getStudent().getFullName());
                        } catch (Exception e) {
                            log.error("Gửi nhắc nhở hạn chót xác nhận phiếu đồng ý thất bại cho phụ huynh ID: {} (email: {}). Lỗi: {}",
                                    parent.getUserId(), parent.getEmail(), e.getMessage());
                        }
                    }
                });
            }
        }
        log.info("Bộ lập lịch: Đã gửi xong tất cả nhắc nhở hạn chót xác nhận phiếu đồng ý.");
    }
}
