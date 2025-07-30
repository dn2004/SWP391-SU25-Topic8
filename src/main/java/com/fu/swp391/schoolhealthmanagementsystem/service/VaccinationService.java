package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.VaccinationCampaignMapper;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.VaccinationConsentMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.ParentStudentLinkRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.VaccinationCampaignRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.VaccinationConsentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.VaccinationCampaignSpecification;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.VaccinationConsentSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaccinationService {

    private final VaccinationCampaignRepository vaccinationCampaignRepository;
    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final StudentRepository studentRepository;
    private final VaccinationCampaignMapper campaignMapper;
    private final VaccinationConsentMapper consentMapper;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;
    private final VaccinationCampaignSpecification campaignSpecification;
    private final VaccinationConsentSpecification consentSpecification;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    /**
     * Tạo mới một chiến dịch tiêm chủng
     * @param requestDto thông tin chiến dịch
     * @return VaccinationCampaignResponseDto
     * @throws InvalidOperationException nếu ngày tiêm chủng không hợp lệ
     */
    @Transactional
    public VaccinationCampaignResponseDto createVaccinationCampaign(CreateVaccinationCampaignRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[VACCINATION] [USER: {}] Yêu cầu tạo chiến dịch tiêm chủng mới: {}", currentUser.getEmail(), requestDto.campaignName());
        if (requestDto.vaccinationDate().isBefore(LocalDate.now())) {
            log.warn("[VACCINATION] Ngày tiêm chủng không hợp lệ (trong quá khứ): {}", requestDto.vaccinationDate());
            throw new InvalidOperationException("Ngày tiêm chủng không được ở trong quá khứ.");
        }
        LocalDate consentDeadline = requestDto.vaccinationDate().minusDays(2);
        VaccinationCampaign campaign = campaignMapper.toEntity(requestDto);
        campaign.setStatus(VaccinationCampaignStatus.DRAFT);
        campaign.setOrganizedByUser(currentUser);
        campaign.setConsentDeadline(consentDeadline);
        VaccinationCampaign savedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] Đã tạo chiến dịch tiêm chủng mới với ID: {}", savedCampaign.getCampaignId());
        VaccinationCampaignResponseDto responseDto = campaignMapper.toDto(savedCampaign);
        responseDto = enrichCampaignWithStatistics(responseDto, 0, 0, 0);
        return responseDto;
    }

    /**
     * Cập nhật thông tin chiến dịch tiêm chủng
     * @param campaignId ID chiến dịch
     * @param requestDto thông tin cập nhật
     * @return VaccinationCampaignResponseDto
     * @throws ResourceNotFoundException nếu không tìm thấy chiến dịch
     * @throws InvalidOperationException nếu trạng thái không hợp lệ
     */
    @Transactional
    public VaccinationCampaignResponseDto updateVaccinationCampaign(Long campaignId, CreateVaccinationCampaignRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[VACCINATION] [USER: {}] Yêu cầu cập nhật chiến dịch ID: {}", currentUser.getEmail(), campaignId);
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });
        if (campaign.getStatus() != VaccinationCampaignStatus.DRAFT) {
            log.warn("[VACCINATION] Không thể cập nhật chiến dịch ID: {} vì không ở trạng thái Nháp.", campaignId);
            throw new InvalidOperationException("Chỉ có thể cập nhật chiến dịch ở trạng thái Nháp.");
        }
        campaignMapper.updateEntityFromDto(requestDto, campaign);
        campaign.setUpdatedByUser(currentUser);
        campaign.setVaccinationDate(requestDto.vaccinationDate().minusDays(2));
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] Đã cập nhật chiến dịch ID: {}", updatedCampaign.getCampaignId());
        VaccinationCampaignResponseDto responseDto = campaignMapper.toDto(updatedCampaign);
        int totalStudents = vaccinationCampaignRepository.countConsentsForCampaign(campaignId);
        int approvedConsents = vaccinationCampaignRepository.countApprovedConsentsForCampaign(campaignId);
        int declinedConsents = vaccinationCampaignRepository.countDeclinedConsentsForCampaign(campaignId);
        return enrichCampaignWithStatistics(responseDto, totalStudents, approvedConsents, declinedConsents);
    }

    /**
     * Lấy thông tin chi tiết của một chiến dịch tiêm chủng
     * @param campaignId ID chiến dịch
     * @return VaccinationCampaignResponseDto
     * @throws ResourceNotFoundException nếu không tìm thấy chiến dịch
     */
    @Transactional(readOnly = true)
    public VaccinationCampaignResponseDto getVaccinationCampaignById(Long campaignId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[VACCINATION] [USER: {}] Yêu cầu xem chi tiết chiến dịch ID: {}", currentUser.getEmail(), campaignId);
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });
        VaccinationCampaignResponseDto responseDto = campaignMapper.toDto(campaign);
        int totalStudents = vaccinationCampaignRepository.countConsentsForCampaign(campaignId);
        int approvedConsents = vaccinationCampaignRepository.countApprovedConsentsForCampaign(campaignId);
        int declinedConsents = vaccinationCampaignRepository.countDeclinedConsentsForCampaign(campaignId);
        return enrichCampaignWithStatistics(responseDto, totalStudents, approvedConsents, declinedConsents);
    }

    // Helper method to enrich campaign response with statistics
    private VaccinationCampaignResponseDto enrichCampaignWithStatistics(
            VaccinationCampaignResponseDto responseDto,
            int totalStudents,
            int approvedConsents,
            int declinedConsents) {
        return new VaccinationCampaignResponseDto(
                responseDto.campaignId(),
                responseDto.campaignName(),
                responseDto.vaccineName(),
                responseDto.description(),
                responseDto.vaccinationDate(),
                responseDto.consentDeadline(),
                responseDto.targetClassGroup(),
                responseDto.status(),
                responseDto.notes(),
                responseDto.organizedByUserId(),
                responseDto.organizedByUserName(),
                responseDto.healthcareProviderName(),
                responseDto.healthcareProviderContact(),
                totalStudents,
                approvedConsents,
                declinedConsents,
                responseDto.createdAt(),
                responseDto.updatedAt(),
                responseDto.updatedByUserId(),
                responseDto.updatedByUserName(),
                responseDto.rescheduledAt(),
                responseDto.rescheduledByUserId(),
                responseDto.rescheduledByUserName()
        );
    }

    // Get all campaigns with pagination
    @Transactional(readOnly = true)
    public Page<VaccinationCampaignResponseDto> getAllVaccinationCampaigns(Pageable pageable) {
        authorizationService.getCurrentUserAndValidate();
        log.info("[VACCINATION] Yêu cầu xem danh sách tất cả các chiến dịch tiêm chủng");
        Page<VaccinationCampaign> campaignPage = vaccinationCampaignRepository.findAll(pageable);
        return campaignPage.map(campaign -> {
            VaccinationCampaignResponseDto dto = campaignMapper.toDto(campaign);
            int totalStudents = vaccinationCampaignRepository.countConsentsForCampaign(campaign.getCampaignId());
            int approvedConsents = vaccinationCampaignRepository.countApprovedConsentsForCampaign(campaign.getCampaignId());
            int declinedConsents = vaccinationCampaignRepository.countDeclinedConsentsForCampaign(campaign.getCampaignId());

            return enrichCampaignWithStatistics(dto, totalStudents, approvedConsents, declinedConsents);
        });
    }

    // ---- START REFACTOR: NEW STATUS CHANGE METHODS ----

    /**
     * Lên lịch cho một chiến dịch (chuyển từ DRAFT sang SCHEDULED).
     *
     * @param campaignId ID của chiến dịch.
     * @return DTO của chiến dịch đã được cập nhật.
     */
    @Transactional
    public VaccinationCampaignResponseDto scheduleCampaign(Long campaignId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        if (campaign.getStatus() != VaccinationCampaignStatus.DRAFT) {
            log.warn("[VACCINATION] Không thể lên lịch chiến dịch ID: {} vì không ở trạng thái Nháp.", campaignId);
            throw new InvalidOperationException("Chỉ có thể lên lịch cho chiến dịch đang ở trạng thái Nháp (DRAFT).");
        }

        // Generate consent forms for all eligible students
        generateConsentFormsForCampaign(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.SCHEDULED);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] [USER: {}] Đã lên lịch chiến dịch tiêm chủng ID: {}", currentUser.getEmail(), campaignId);

        // Send notifications to parents
        sendConsentNotificationsToParents(updatedCampaign);

        return enrichCampaignWithFullStatistics(updatedCampaign);
    }

    /**
     * Bắt đầu một chiến dịch (chuyển từ PREPARING sang IN_PROGRESS).
     *
     * @param campaignId ID của chiến dịch.
     * @return DTO của chiến dịch đã được cập nhật.
     */
    @Transactional
    public VaccinationCampaignResponseDto startCampaign(Long campaignId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        if (campaign.getStatus() != VaccinationCampaignStatus.PREPARING) {
            log.warn("[VACCINATION] Không thể bắt đầu chiến dịch ID: {} vì không ở trạng thái Chuẩn bị.", campaignId);
            throw new InvalidOperationException("Chỉ có thể bắt đầu chiến dịch đang ở trạng thái Chuẩn bị (PREPARING).");
        }

        if (LocalDate.now().isBefore(campaign.getVaccinationDate())) {
            log.warn("[VACCINATION] Không thể bắt đầu chiến dịch trước ngày tiêm chủng đã lên lịch.");
            throw new InvalidOperationException("Không thể bắt đầu chiến dịch trước ngày tiêm chủng đã lên lịch.");
        }

        // Notify medical staff that campaign is now active
        notifyMedicalStaffCampaignStarted(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.IN_PROGRESS);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] [USER: {}] Đã bắt đầu chiến dịch tiêm chủng ID: {}", currentUser.getEmail(), campaignId);

        return enrichCampaignWithFullStatistics(updatedCampaign);
    }

    /**
     * Hoàn thành một chiến dịch (chuyển từ IN_PROGRESS sang COMPLETED).
     *
     * @param campaignId ID của chiến dịch.
     * @return DTO của chiến dịch đã được cập nhật.
     */
    @Transactional
    public VaccinationCampaignResponseDto completeCampaign(Long campaignId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        if (campaign.getStatus() != VaccinationCampaignStatus.IN_PROGRESS) {
            log.warn("[VACCINATION] Không thể hoàn thành chiến dịch ID: {} vì không ở trạng thái Đang diễn ra.", campaignId);
            throw new InvalidOperationException("Chỉ có thể hoàn thành chiến dịch đang ở trạng thái Đang diễn ra (IN_PROGRESS).");
        }

        // Verify all vaccinations are done
        verifyAllVaccinationsCompleted(campaignId);
        // Notify about campaign completion
        notifyAboutCampaignCompletion(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.COMPLETED);

        List<SchoolVaccination> scheduledVaccination = campaign.getVaccinations().stream()
                .filter(sv -> sv.getStatus() == SchoolVaccinationStatus.SCHEDULED)
                .toList();
        scheduledVaccination.forEach(schoolVaccination -> {
            log.info("Bộ lập lịch: Cập nhật trạng thái tiêm chủng ID {} từ SCHEDULED sang ABSENT.", schoolVaccination.getSchoolVaccinationId());
            schoolVaccination.setStatus(SchoolVaccinationStatus.ABSENT);
        });

        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] [USER: {}] Đã hoàn thành chiến dịch tiêm chủng ID: {}", currentUser.getEmail(), campaignId);

        return enrichCampaignWithFullStatistics(updatedCampaign);
    }

    /**
     * Hủy một chiến dịch.
     *
     * @param campaignId ID của chiến dịch.
     * @return DTO của chiến dịch đã được cập nhật.
     */
    @Transactional
    public VaccinationCampaignResponseDto cancelCampaign(Long campaignId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        VaccinationCampaignStatus currentStatus = campaign.getStatus();
        if (currentStatus == VaccinationCampaignStatus.COMPLETED || currentStatus == VaccinationCampaignStatus.CANCELED) {
            log.warn("[VACCINATION] Không thể hủy chiến dịch ID: {} vì đã hoàn thành hoặc đã bị hủy.", campaignId);
            throw new InvalidOperationException("Không thể hủy chiến dịch đã Hoàn thành (COMPLETED) hoặc đã bị Hủy (CANCELED).");
        }

        // Notify about campaign cancellation
        notifyAboutCampaignCancellation(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.CANCELED);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] [USER: {}] Đã hủy chiến dịch tiêm chủng ID: {}", currentUser.getEmail(), campaignId);

        return enrichCampaignWithFullStatistics(updatedCampaign);
    }

    // Helper to enrich DTO with stats
    private VaccinationCampaignResponseDto enrichCampaignWithFullStatistics(VaccinationCampaign campaign) {
        VaccinationCampaignResponseDto dto = campaignMapper.toDto(campaign);
        int totalStudents = vaccinationCampaignRepository.countConsentsForCampaign(campaign.getCampaignId());
        int approvedConsents = vaccinationCampaignRepository.countApprovedConsentsForCampaign(campaign.getCampaignId());
        int declinedConsents = vaccinationCampaignRepository.countDeclinedConsentsForCampaign(campaign.getCampaignId());
        return enrichCampaignWithStatistics(dto, totalStudents, approvedConsents, declinedConsents);
    }

    // ---- END REFACTOR ----

    /**
     * Gửi thông báo phiếu đồng ý tiêm chủng cho phụ huynh
     */
    private void sendConsentNotificationsToParents(VaccinationCampaign campaign) {
        log.info("[VACCINATION] Bắt đầu gửi thông báo phiếu đồng ý cho phụ huynh chiến dịch ID: {}", campaign.getCampaignId());
        List<VaccinationConsent> consents = vaccinationConsentRepository.findByCampaign(campaign);
        consents.forEach(consent -> {
            List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudent(consent.getStudent());
            parentLinks.forEach(parentLink -> {
                User parent = parentLink.getParent();
                if (parent != null) {
                    String content = String.format(
                            "Phiếu đồng ý tiêm chủng '%s' cho học sinh %s đã được gửi. Vui lòng xác nhận trước ngày %s.",
                            campaign.getCampaignName(), consent.getStudent().getFullName(),
                            campaign.getConsentDeadline().toString());
                    String link = "/vaccination/consent/" + consent.getConsentId();
                    try {
                        notificationService.createAndSendNotification(
                                parent.getEmail(), content, link, campaign.getOrganizedByUser().getEmail());
                        log.info("[VACCINATION] Đã gửi thông báo phiếu đồng ý cho học sinh ID: {} tới phụ huynh: {}",
                                consent.getStudent().getId(), parent.getEmail());
                    } catch (Exception e) {
                        log.error("[VACCINATION] Lỗi gửi thông báo phiếu đồng ý cho phụ huynh ID: {}, Email: {}. Lỗi: {}",
                                parent.getUserId(), parent.getEmail(), e.getMessage());
                    }
                }
            });
        });
    }

    /**
     * Gửi nhắc nhở phiếu đồng ý tiêm chủng cho phụ huynh chưa phản hồi
     */
    @Transactional
    public void sendConsentReminderNotifications(Long campaignId) {
        log.info("[VACCINATION] Bắt đầu gửi nhắc nhở phiếu đồng ý cho chiến dịch ID: {}", campaignId);
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId));
        if (campaign.getStatus() != VaccinationCampaignStatus.SCHEDULED) {
            log.info("[VACCINATION] Chiến dịch ID {} không ở trạng thái Đã lên lịch, bỏ qua gửi nhắc nhở.", campaignId);
            return;
        }
        List<VaccinationConsent> pendingConsents = vaccinationConsentRepository.findPendingConsentsWithNoReminder(campaignId);
        log.info("[VACCINATION] Có {} phiếu đồng ý chưa nhắc nhở cho chiến dịch ID: {}", pendingConsents.size(), campaignId);
        pendingConsents.forEach(consent -> {
            List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudent(consent.getStudent());
            parentLinks.forEach(parentLink -> {
                User parent = parentLink.getParent();
                if (parent != null) {
                    String content = String.format(
                            "Nhắc nhở: Vui lòng xác nhận phiếu đồng ý tiêm chủng '%s' cho học sinh %s trước ngày %s.",
                            campaign.getCampaignName(), consent.getStudent().getFullName(),
                            campaign.getConsentDeadline().toString());
                    String link = "/vaccination/consent/" + consent.getConsentId();
                    try {
                        notificationService.createAndSendNotification(
                                parent.getEmail(), content, link, campaign.getOrganizedByUser().getEmail());
                        consent.setReminderSentAt(LocalDateTime.now());
                        vaccinationConsentRepository.save(consent);
                        log.info("[VACCINATION] Đã gửi nhắc nhở phiếu đồng ý cho học sinh ID: {} tới phụ huynh: {}",
                                consent.getStudent().getId(), parent.getEmail());
                    } catch (Exception e) {
                        log.error("[VACCINATION] Lỗi gửi nhắc nhở phiếu đồng ý cho phụ huynh ID: {}, Email: {}. Lỗi: {}",
                                parent.getUserId(), parent.getEmail(), e.getMessage());
                    }
                }
            });
        });
    }

    /**
     * Gửi thông báo cho nhân viên y tế về giai đoạn chuẩn bị chiến dịch
     */
    private void notifyMedicalStaffAboutPreparation(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã chuyển sang giai đoạn chuẩn bị. Vui lòng chuẩn bị sẵn sàng cho ngày %s.",
                campaign.getCampaignName(), campaign.getVaccinationDate().toString());
        String link = "/vaccination/campaigns/" + campaign.getCampaignId();
        try {
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());
            log.info("[VACCINATION] Đã gửi thông báo chuẩn bị chiến dịch tới nhân viên y tế cho chiến dịch ID: {}",
                    campaign.getCampaignId());
        } catch (Exception e) {
            log.error("[VACCINATION] Lỗi gửi thông báo chuẩn bị chiến dịch tới nhân viên y tế cho chiến dịch ID: {}. Lỗi: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    /**
     * Gửi thông báo cho nhân viên y tế khi bắt đầu chiến dịch
     */
    private void notifyMedicalStaffCampaignStarted(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã bắt đầu. Vui lòng thực hiện tiêm chủng theo kế hoạch.",
                campaign.getCampaignName());
        String link = "/vaccination/campaigns/" + campaign.getCampaignId();
        try {
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());
            log.info("[VACCINATION] Đã gửi thông báo bắt đầu chiến dịch tới nhân viên y tế cho chiến dịch ID: {}",
                    campaign.getCampaignId());
        } catch (Exception e) {
            log.error("[VACCINATION] Lỗi gửi thông báo bắt đầu chiến dịch tới nhân viên y tế cho chiến dịch ID: {}. Lỗi: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    /**
     * Gửi thông báo hoàn thành chiến dịch
     */
    private void notifyAboutCampaignCompletion(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã hoàn thành.",
                campaign.getCampaignName());
        String link = "/vaccination/campaigns/" + campaign.getCampaignId();
        try {
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());
            notificationService.createAndSendNotificationToRole(
                    UserRole.StaffManager, content, link, currentUser.getEmail());
            log.info("[VACCINATION] Đã gửi thông báo hoàn thành chiến dịch cho ID: {}", campaign.getCampaignId());
        } catch (Exception e) {
            log.error("[VACCINATION] Lỗi gửi thông báo hoàn thành chiến dịch cho ID: {}. Lỗi: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    /**
     * Gửi thông báo hủy chiến dịch
     */
    private void notifyAboutCampaignCancellation(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã bị hủy.",
                campaign.getCampaignName());
        String link = "/vaccination/campaigns/" + campaign.getCampaignId();
        try {
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());
            notificationService.createAndSendNotificationToRole(
                    UserRole.StaffManager, content, link, currentUser.getEmail());
            List<VaccinationConsent> consents = vaccinationConsentRepository.findByCampaign(campaign);
            consents.forEach(consent -> {
                List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudent(consent.getStudent());
                parentLinks.forEach(parentLink -> {
                    User parent = parentLink.getParent();
                    if (parent != null) {
                        String parentContent = String.format(
                                "Chiến dịch tiêm chủng '%s' cho học sinh %s đã bị hủy.",
                                campaign.getCampaignName(), consent.getStudent().getFullName());
                        notificationService.createAndSendNotification(
                                parent.getEmail(), parentContent, link, currentUser.getEmail());
                    }
                });
            });
            log.info("[VACCINATION] Đã gửi thông báo hủy chiến dịch cho ID: {}", campaign.getCampaignId());
        } catch (Exception e) {
            log.error("[VACCINATION] Lỗi gửi thông báo hủy chiến dịch cho ID: {}. Lỗi: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    /**
     * Tạo phiếu đồng ý tiêm chủng cho tất cả học sinh mục tiêu
     */
    @Transactional
    public void generateConsentFormsForCampaign(VaccinationCampaign campaign, User currentUser) {
        log.info("[VACCINATION] Bắt đầu tạo phiếu đồng ý cho chiến dịch ID: {}", campaign.getCampaignId());
        List<Student> targetStudents = findTargetStudentsForCampaign(campaign);
        List<VaccinationConsent> consentsToCreate = new ArrayList<>();
        for (Student student : targetStudents) {
            boolean consentExists = vaccinationConsentRepository.findByCampaignAndStudent(campaign, student).isPresent();
            if (!consentExists) {
                VaccinationConsent consent = new VaccinationConsent();
                consent.setCampaign(campaign);
                consent.setStudent(student);
                consent.setStatus(ConsentStatus.PENDING);
                consent.setConsentFormSentAt(LocalDateTime.now());
                consentsToCreate.add(consent);
            }
        }
        if (!consentsToCreate.isEmpty()) {
            vaccinationConsentRepository.saveAll(consentsToCreate);
            log.info("[VACCINATION] Đã tạo {} phiếu đồng ý cho chiến dịch ID: {}", consentsToCreate.size(), campaign.getCampaignId());
        }
    }

    /**
     * Tìm kiếm học sinh mục tiêu dựa trên khối lớp hoặc trạng thái hoạt động
     */
    private List<Student> findTargetStudentsForCampaign(VaccinationCampaign campaign) {
        if (campaign.getTargetClassGroup() != null) {
            ClassGroup targetGroup = campaign.getTargetClassGroup();
            log.info("[VACCINATION] Tìm học sinh mục tiêu theo khối lớp: {}", targetGroup);
            return studentRepository.findByClassGroupAndStatus(targetGroup, StudentStatus.ACTIVE);
        }
        log.info("[VACCINATION] Không có tiêu chí khối lớp, lấy tất cả học sinh đang hoạt động");
        return studentRepository.findByStatus(StudentStatus.ACTIVE);
    }

    // Get all consents for a campaign with pagination
    @Transactional(readOnly = true)
    public Page<VaccinationConsentResponseDto> getConsentsForCampaign(Long campaignId, Pageable pageable) {
        authorizationService.getCurrentUserAndValidate();

        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        Page<VaccinationConsent> consentsPage = vaccinationConsentRepository.findByCampaign(campaign, pageable);
        return consentsPage.map(consentMapper::toDto);
    }

    // Get all consents for a campaign with pagination and filtering
    @Transactional(readOnly = true)
    public Page<VaccinationConsentResponseDto> getConsentsForCampaign(Long campaignId, Pageable pageable, String
            studentName, String className) {
        authorizationService.getCurrentUserAndValidate();

        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        Specification<VaccinationConsent> spec = Specification.allOf(
                consentSpecification.forCampaign(campaign),
                consentSpecification.hasStudentNameContaining(studentName),
                consentSpecification.hasClassNameContaining(className)
        );

        Page<VaccinationConsent> consentsPage = vaccinationConsentRepository.findAll(spec, pageable);
        return consentsPage.map(consentMapper::toDto);
    }

    // Get a consent by ID
    @Transactional(readOnly = true)
    public VaccinationConsentResponseDto getConsentById(Long consentId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        VaccinationConsent consent = vaccinationConsentRepository.findById(consentId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy phiếu đồng ý với ID: {}", consentId);
                    return new ResourceNotFoundException("Không tìm thấy phiếu đồng ý với ID: " + consentId);
                });

        Student student = consent.getStudent();

        // If user is a parent, verify they are a parent of this student with ACTIVE status
        if (currentUser.getRole() == UserRole.Parent) {
            try {
                authorizationService.authorizeParentAction(currentUser, student, "xem phiếu đồng ý tiêm chủng");
            } catch (AccessDeniedException e) {
                log.warn("[VACCINATION] Phụ huynh {} cố truy cập phiếu đồng ý ID {} cho học sinh {} không hợp lệ.",
                        currentUser.getEmail(), consentId, student.getId());
                throw new AccessDeniedException("Bạn không có quyền xem phiếu đồng ý tiêm chủng này.");
            }
        }
        log.info("[VACCINATION] Người dùng {} truy cập phiếu đồng ý ID: {} cho học sinh: {}",
                currentUser.getEmail(), consentId, student.getFullName());
        return consentMapper.toDto(consent);
    }

    // Update consent response from parent
    @Transactional
    public VaccinationConsentResponseDto updateConsentResponse(
            Long consentId, UpdateVaccinationConsentRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        VaccinationConsent consent = vaccinationConsentRepository.findById(consentId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy phiếu đồng ý với ID: {}", consentId);
                    return new ResourceNotFoundException("Không tìm thấy phiếu đồng ý với ID: " + consentId);
                });

        // Check if campaign is still accepting consent responses
        VaccinationCampaign campaign = consent.getCampaign();
        if (campaign.getStatus() != VaccinationCampaignStatus.SCHEDULED) {
            log.warn("[VACCINATION] Chiến dịch ID: {} không còn nhận phản hồi đồng ý.", campaign.getCampaignId());
            throw new InvalidOperationException("Chiến dịch không còn nhận phản hồi đồng ý.");
        }
        if (LocalDate.now().isAfter(campaign.getConsentDeadline())) {
            log.warn("[VACCINATION] Đã quá hạn phản hồi đồng ý cho chiến dịch ID: {}", campaign.getCampaignId());
            throw new InvalidOperationException("Đã quá hạn phản hồi đồng ý.");
        }

        // Store old status for notification
        ConsentStatus oldStatus = consent.getStatus();

        // Update consent
        consentMapper.updateEntityFromDto(requestDto, consent);
        consent.setResponseReceivedAt(LocalDateTime.now());

        VaccinationConsent updatedConsent = vaccinationConsentRepository.save(consent);
        log.info("[VACCINATION] Đã cập nhật trạng thái phiếu đồng ý ID: {} thành {} bởi nhân viên {}",
                consentId, updatedConsent.getStatus(), currentUser.getEmail());

        // Send notification to medical staff about consent response
        List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudent(consent.getStudent());
        parentLinks.forEach(link -> {
            User parent = link.getParent();
            if (parent != null) {
                notifyStaffAboutConsentResponse(updatedConsent, oldStatus, parent);
            }
        });
        return consentMapper.toDto(updatedConsent);
    }

    // Notify medical staff about consent response
    private void notifyStaffAboutConsentResponse(VaccinationConsent consent, ConsentStatus oldStatus, User parent) {
        if (consent.getStatus() == oldStatus) {
            return; // No status change, no notification needed
        }

        VaccinationCampaign campaign = consent.getCampaign();
        Student student = consent.getStudent();

        String statusText = consent.getStatus() == ConsentStatus.APPROVED ? "đồng ý" : "từ chối";
        String content = String.format(
                "Phụ huynh %s đã %s cho học sinh %s tham gia tiêm chủng '%s'.",
                parent.getFullName(), statusText, student.getFullName(), campaign.getCampaignName());

        String link = "/vaccination/campaigns/" + campaign.getCampaignId() + "/consents";

        try {
            User organizer = campaign.getOrganizedByUser();
            if (organizer != null) {
                notificationService.createAndSendNotification(
                        organizer.getEmail(), content, link, "hệ thống");
            }
            log.info("[VACCINATION] Đã gửi thông báo phản hồi đồng ý cho học sinh ID: {} đến người tổ chức chiến dịch.",
                    student.getId());
        } catch (Exception e) {
            log.error("[VACCINATION] Gửi thông báo phản hồi đồng ý thất bại cho học sinh ID: {}. Lỗi: {}",
                    student.getId(), e.getMessage());
        }
    }

    // Verify all vaccinations are completed
    private void verifyAllVaccinationsCompleted(Long campaignId) {
        log.info("[VACCINATION] Đang kiểm tra hoàn thành tiêm chủng cho chiến dịch ID: {}", campaignId);
    }

    // Get all campaigns with pagination and filtering
    @Transactional(readOnly = true)
    public Page<VaccinationCampaignResponseDto> getAllVaccinationCampaigns(
            Pageable pageable,
            String campaignName,
            String vaccineName,
            VaccinationCampaignStatus status,
            LocalDate startDate,
            LocalDate endDate,
            ClassGroup classGroup,
            Long organizedByUserId) {

        authorizationService.getCurrentUserAndValidate();

        Specification<VaccinationCampaign> spec = Specification
                .allOf(
                        campaignSpecification.hasName(campaignName),
                        campaignSpecification.hasVaccineName(vaccineName),
                        campaignSpecification.hasStatus(status),
                        campaignSpecification.vaccinationDateAfterOrEqual(startDate),
                        campaignSpecification.vaccinationDateBeforeOrEqual(endDate),
                        campaignSpecification.hasClassGroup(classGroup),
                        campaignSpecification.organizedBy(organizedByUserId)
                );

        Page<VaccinationCampaign> campaignPage = vaccinationCampaignRepository.findAll(spec, pageable);
        return campaignPage.map(campaign -> {
            VaccinationCampaignResponseDto dto = campaignMapper.toDto(campaign);
            int totalStudents = vaccinationCampaignRepository.countConsentsForCampaign(campaign.getCampaignId());
            int approvedConsents = vaccinationCampaignRepository.countApprovedConsentsForCampaign(campaign.getCampaignId());
            int declinedConsents = vaccinationCampaignRepository.countDeclinedConsentsForCampaign(campaign.getCampaignId());

            return enrichCampaignWithStatistics(dto, totalStudents, approvedConsents, declinedConsents);
        });
    }

    /**
     * Dời lịch một chiến dịch đang ở trạng thái PREPARING.
     *
     * @param campaignId ID của chiến dịch.
     * @param requestDto Thông tin ngày mới và lý do dời lịch.
     * @return DTO của chiến dịch đã được cập nhật.
     */
    @Transactional
    public VaccinationCampaignResponseDto rescheduleCampaign(Long campaignId, RescheduleCampaignRequestDto
            requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> {
                    log.warn("[VACCINATION] Không tìm thấy chiến dịch với ID: {}", campaignId);
                    return new ResourceNotFoundException("Không tìm thấy chiến dịch tiêm chủng với ID: " + campaignId);
                });

        if (campaign.getStatus() != VaccinationCampaignStatus.PREPARING) {
            log.warn("[VACCINATION] Không thể dời lịch chiến dịch ID: {} vì không ở trạng thái Chuẩn bị.", campaignId);
            throw new InvalidOperationException("Chỉ có thể dời lịch cho chiến dịch đang ở trạng thái Chuẩn bị.");
        }

        LocalDate newDate = requestDto.newVaccinationDate();
        LocalDate currentDate = campaign.getVaccinationDate();

        // Kiểm tra ngày mới phải trong hôm nay hoặc tương lai
        if (newDate.isBefore(LocalDate.now())) {
            log.warn("[VACCINATION] Ngày tiêm chủng mới không hợp lệ: {}", newDate);
            throw new InvalidOperationException("Ngày tiêm chủng mới phải từ hôm nay trở đi.");
        }
        if (newDate.equals(currentDate)) {
            log.warn("[VACCINATION] Ngày tiêm chủng mới trùng với ngày hiện tại.");
            throw new InvalidOperationException("Ngày tiêm chủng mới phải khác với ngày hiện tại.");
        }

        // Cập nhật ngày tiêm chủng và thông tin dời lịch
        campaign.setVaccinationDate(newDate);
        campaign.setRescheduledAt(LocalDateTime.now());
        campaign.setRescheduledByUser(currentUser);
        campaign.setUpdatedByUser(currentUser);

        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("[VACCINATION] [USER: {}] Đã dời lịch chiến dịch tiêm chủng ID: {} từ {} sang {}",
                currentUser.getEmail(), campaignId, currentDate, newDate);

        // Gửi thông báo cho phụ huynh về việc thay đổi lịch
        notifyParentsAboutReschedule(updatedCampaign, currentDate, requestDto.reason());

        return enrichCampaignWithFullStatistics(updatedCampaign);
    }

    // Notify parents about campaign reschedule
    private void notifyParentsAboutReschedule(VaccinationCampaign campaign, LocalDate oldDate, String reason) {
        List<VaccinationConsent> consents = vaccinationConsentRepository.findByCampaign(campaign);
        consents.forEach(consent -> {
            List<ParentStudentLink> parentLinks = parentStudentLinkRepository.findByStudent(consent.getStudent());
            parentLinks.forEach(parentLink -> {
                User parent = parentLink.getParent();
                if (parent != null) {
                    String reasonText = (reason != null && !reason.trim().isEmpty())
                            ? " Lý do: " + reason
                            : "";

                    String content = String.format(
                            "Thông báo thay đổi lịch: Chiến dịch tiêm chủng '%s' cho học sinh %s đã được dời từ ngày %s sang ngày %s.%s",
                            campaign.getCampaignName(),
                            consent.getStudent().getFullName(),
                            oldDate.toString(),
                            campaign.getVaccinationDate().toString(),
                            reasonText);

                    String link = "/vaccination/consent/" + consent.getConsentId();

                    try {
                        notificationService.createAndSendNotification(
                                parent.getEmail(), content, link, campaign.getRescheduledByUser().getEmail());

                        log.info("[VACCINATION] Đã gửi thông báo dời lịch cho học sinh ID: {} đến phụ huynh: {}",
                                consent.getStudent().getId(), parent.getEmail());
                    } catch (Exception e) {
                        log.error("[VACCINATION] Gửi thông báo dời lịch thất bại cho phụ huynh ID: {}, Email: {}. Lỗi: {}",
                                parent.getUserId(), parent.getEmail(), e.getMessage());
                    }
                }
            });
        });
    }
}
