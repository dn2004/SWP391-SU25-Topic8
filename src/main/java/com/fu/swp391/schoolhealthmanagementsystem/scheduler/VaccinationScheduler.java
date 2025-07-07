package com.fu.swp391.schoolhealthmanagementsystem.scheduler;

import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationCampaign;
import com.fu.swp391.schoolhealthmanagementsystem.entity.VaccinationConsent;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
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

    /**
     * Chạy vào 1 giờ sáng mỗi ngày.
     * Tự động xử lý các phiếu đồng ý đã quá hạn.
     * - Tìm các chiến dịch đang ở trạng thái SCHEDULED mà đã qua hạn chót.
     * - Chuyển các phiếu đồng ý PENDING thành DECLINED.
     * - Chuyển trạng thái chiến dịch sang PREPARING.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void processOverdueConsents() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Scheduler: Running job to process overdue consents for deadline date: {}", yesterday);

        List<VaccinationCampaign> overdueCampaigns = campaignRepository.findByStatusAndConsentDeadline(
                VaccinationCampaignStatus.SCHEDULED, yesterday);

        if (overdueCampaigns.isEmpty()) {
            log.info("Scheduler: No overdue campaigns found.");
            return;
        }

        log.info("Scheduler: Found {} overdue campaigns. Processing...", overdueCampaigns.size());
        for (VaccinationCampaign campaign : overdueCampaigns) {
            List<VaccinationConsent> pendingConsents = consentRepository.findByCampaignAndStatus(campaign, ConsentStatus.PENDING);

            if (!pendingConsents.isEmpty()) {
                log.info("Scheduler: Found {} pending consents for campaign ID {}. Setting them to DECLINED.",
                        pendingConsents.size(), campaign.getCampaignId());
                for (VaccinationConsent consent : pendingConsents) {
                    consent.setStatus(ConsentStatus.DECLINED);
                    consent.setResponseReceivedAt(LocalDateTime.now());
                }
                consentRepository.saveAll(pendingConsents);
            }

            log.info("Scheduler: Updating campaign ID {} status from SCHEDULED to PREPARING.", campaign.getCampaignId());
            campaign.setStatus(VaccinationCampaignStatus.PREPARING);
            campaignRepository.save(campaign);
        }
        log.info("Scheduler: Finished processing overdue consents.");
    }

    /**
     * Chạy vào 2 giờ sáng mỗi ngày.
     * Tự động hoàn thành các chiến dịch đã qua ngày tiêm chủng.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void autoCompleteCampaigns() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("Scheduler: Running job to auto-complete campaigns for vaccination date: {}", yesterday);

        List<VaccinationCampaign> completedCampaigns = campaignRepository.findByStatusAndVaccinationDate(
                VaccinationCampaignStatus.IN_PROGRESS, yesterday);

        if (completedCampaigns.isEmpty()) {
            log.info("Scheduler: No campaigns to auto-complete.");
            return;
        }

        log.info("Scheduler: Found {} campaigns to auto-complete. Processing...", completedCampaigns.size());
        for (VaccinationCampaign campaign : completedCampaigns) {
            log.info("Scheduler: Updating campaign ID {} status from IN_PROGRESS to COMPLETED.", campaign.getCampaignId());
            campaign.setStatus(VaccinationCampaignStatus.COMPLETED);
            campaignRepository.save(campaign);
        }
        log.info("Scheduler: Finished auto-completing campaigns.");
    }

    /**
     * Chạy vào 9 giờ sáng mỗi ngày.
     * Gửi thông báo nhắc nhở cho các phụ huynh chưa phản hồi khi gần đến hạn chót.
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendDeadlineReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        log.info("Scheduler: Running job to send deadline reminders for consent deadline: {}", tomorrow);

        List<VaccinationCampaign> campaignsWithUpcomingDeadline = campaignRepository.findByStatusAndConsentDeadline(
                VaccinationCampaignStatus.SCHEDULED, tomorrow);

        if (campaignsWithUpcomingDeadline.isEmpty()) {
            log.info("Scheduler: No campaigns with upcoming deadlines found.");
            return;
        }

        log.info("Scheduler: Found {} campaigns with upcoming deadlines. Processing...", campaignsWithUpcomingDeadline.size());
        for (VaccinationCampaign campaign : campaignsWithUpcomingDeadline) {
            List<VaccinationConsent> consentsToRemind = consentRepository.findByCampaignAndStatus(campaign, ConsentStatus.PENDING);

            log.info("Scheduler: Sending {} reminders for campaign ID {}.", consentsToRemind.size(), campaign.getCampaignId());
            for (VaccinationConsent consent : consentsToRemind) {
                User parent = consent.getParent();
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
                    } catch (Exception e) {
                        log.error("Scheduler: Failed to send reminder to parent ID: {}. Error: {}", parent.getUserId(), e.getMessage());
                    }
                }
            }
        }
        log.info("Scheduler: Finished sending deadline reminders.");
    }
}

