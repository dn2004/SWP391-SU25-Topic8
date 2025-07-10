package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.VaccinationCampaignMapper;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.VaccinationConsentMapper;
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

    // Create a new vaccination campaign
    @Transactional
    public VaccinationCampaignResponseDto createVaccinationCampaign(CreateVaccinationCampaignRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        if (requestDto.vaccinationDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Vaccination date cannot be in the past");
        }

        LocalDate consentDeadline = requestDto.vaccinationDate().minusDays(2);

        VaccinationCampaign campaign = campaignMapper.toEntity(requestDto);
        campaign.setStatus(VaccinationCampaignStatus.DRAFT);
        campaign.setOrganizedByUser(currentUser);
        campaign.setConsentDeadline(consentDeadline);

        VaccinationCampaign savedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} created a new vaccination campaign: {}", currentUser.getEmail(), savedCampaign.getCampaignId());

        VaccinationCampaignResponseDto responseDto = campaignMapper.toDto(savedCampaign);
        // Set counts to zero for a new campaign
        responseDto = enrichCampaignWithStatistics(responseDto, 0, 0, 0);

        return responseDto;
    }

    // Update a campaign
    @Transactional
    public VaccinationCampaignResponseDto updateVaccinationCampaign(Long campaignId, CreateVaccinationCampaignRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        if (campaign.getStatus() != VaccinationCampaignStatus.DRAFT) {
            throw new InvalidOperationException("Can only update campaigns in DRAFT status");
        }

        campaignMapper.updateEntityFromDto(requestDto, campaign);
        campaign.setUpdatedByUser(currentUser);

        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} updated vaccination campaign: {}", currentUser.getEmail(), updatedCampaign.getCampaignId());

        VaccinationCampaignResponseDto responseDto = campaignMapper.toDto(updatedCampaign);
        // Get current statistics
        int totalStudents = vaccinationCampaignRepository.countConsentsForCampaign(campaignId);
        int approvedConsents = vaccinationCampaignRepository.countApprovedConsentsForCampaign(campaignId);
        int declinedConsents = vaccinationCampaignRepository.countDeclinedConsentsForCampaign(campaignId);

        return enrichCampaignWithStatistics(responseDto, totalStudents, approvedConsents, declinedConsents);
    }

    // Get a campaign by ID
    @Transactional(readOnly = true)
    public VaccinationCampaignResponseDto getVaccinationCampaignById(Long campaignId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        VaccinationCampaignResponseDto responseDto = campaignMapper.toDto(campaign);
        // Get current statistics
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
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        if (campaign.getStatus() != VaccinationCampaignStatus.DRAFT) {
            throw new InvalidOperationException("Chỉ có thể lên lịch cho chiến dịch đang ở trạng thái Nháp (DRAFT).");
        }

        // Generate consent forms for all eligible students
        generateConsentFormsForCampaign(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.SCHEDULED);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} scheduled vaccination campaign {}", currentUser.getEmail(), campaignId);

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
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        if (campaign.getStatus() != VaccinationCampaignStatus.PREPARING) {
            throw new InvalidOperationException("Chỉ có thể bắt đầu chiến dịch đang ở trạng thái Chuẩn bị (PREPARING).");
        }

        if (LocalDate.now().isBefore(campaign.getVaccinationDate())) {
            throw new InvalidOperationException("Không thể bắt đầu chiến dịch trước ngày tiêm chủng đã lên lịch.");
        }

        // Notify medical staff that campaign is now active
        notifyMedicalStaffCampaignStarted(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.IN_PROGRESS);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} started vaccination campaign {}", currentUser.getEmail(), campaignId);

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
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        if (campaign.getStatus() != VaccinationCampaignStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Chỉ có thể hoàn thành chiến dịch đang ở trạng thái Đang diễn ra (IN_PROGRESS).");
        }

        // Verify all vaccinations are done
        verifyAllVaccinationsCompleted(campaignId);
        // Notify about campaign completion
        notifyAboutCampaignCompletion(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.COMPLETED);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} completed vaccination campaign {}", currentUser.getEmail(), campaignId);

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
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        VaccinationCampaignStatus currentStatus = campaign.getStatus();
        if (currentStatus == VaccinationCampaignStatus.COMPLETED || currentStatus == VaccinationCampaignStatus.CANCELED) {
            throw new InvalidOperationException("Không thể hủy chiến dịch đã Hoàn thành (COMPLETED) hoặc đã bị Hủy (CANCELED).");
        }

        // Notify about campaign cancellation
        notifyAboutCampaignCancellation(campaign, currentUser);

        campaign.setStatus(VaccinationCampaignStatus.CANCELED);
        campaign.setUpdatedByUser(currentUser);
        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} canceled vaccination campaign {}", currentUser.getEmail(), campaignId);

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

    // Send notifications to parents
    private void sendConsentNotificationsToParents(VaccinationCampaign campaign) {
        List<VaccinationConsent> consents = vaccinationConsentRepository.findByCampaign(campaign);

        for (VaccinationConsent consent : consents) {
            User parent = consent.getParent();
            if (parent != null) {
                String content = String.format(
                        "Phiếu đồng ý tiêm chủng '%s' cho học sinh %s đã được gửi. Vui lòng xác nhận trước ngày %s.",
                        campaign.getCampaignName(), consent.getStudent().getFullName(),
                        campaign.getConsentDeadline().toString());

                String link = "/vaccination/consent/" + consent.getConsentId();

                try {
                    notificationService.createAndSendNotification(
                            parent.getEmail(), content, link, campaign.getOrganizedByUser().getEmail());

                    log.info("Sent consent notification for student ID: {} to parent: {}",
                            consent.getStudent().getId(), parent.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send consent notification to parent ID: {}, Email: {}. Error: {}",
                            parent.getUserId(), parent.getEmail(), e.getMessage());
                }
            }
        }
    }

    // Send reminder notifications to parents who haven't responded
    @Transactional
    public void sendConsentReminderNotifications(Long campaignId) {
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        if (campaign.getStatus() != VaccinationCampaignStatus.SCHEDULED) {
            log.info("Campaign ID {} is not in SCHEDULED status, skipping consent reminders", campaignId);
            return;
        }

        // Find all pending consents with no reminder yet
        List<VaccinationConsent> pendingConsents = vaccinationConsentRepository.findPendingConsentsWithNoReminder(campaignId);
        log.info("Found {} pending consents without reminders for campaign ID: {}", pendingConsents.size(), campaignId);

        for (VaccinationConsent consent : pendingConsents) {
            User parent = consent.getParent();
            if (parent != null) {
                String content = String.format(
                        "Nhắc nhở: Vui lòng xác nhận phiếu đồng ý tiêm chủng '%s' cho học sinh %s trước ngày %s.",
                        campaign.getCampaignName(), consent.getStudent().getFullName(),
                        campaign.getConsentDeadline().toString());

                String link = "/vaccination/consent/" + consent.getConsentId();

                try {
                    notificationService.createAndSendNotification(
                            parent.getEmail(), content, link, campaign.getOrganizedByUser().getEmail());

                    // Mark reminder as sent
                    consent.setReminderSentAt(LocalDateTime.now());
                    vaccinationConsentRepository.save(consent);

                    log.info("Sent consent reminder for student ID: {} to parent: {}",
                            consent.getStudent().getId(), parent.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send consent reminder to parent ID: {}, Email: {}. Error: {}",
                            parent.getUserId(), parent.getEmail(), e.getMessage());
                }
            }
        }
    }

    // Notify medical staff about campaign preparation phase
    private void notifyMedicalStaffAboutPreparation(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã chuyển sang giai đoạn chuẩn bị. Vui lòng chuẩn bị sẵn sàng cho ngày %s.",
                campaign.getCampaignName(), campaign.getVaccinationDate().toString());

        String link = "/vaccination/campaigns/" + campaign.getCampaignId();

        try {
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            log.info("Sent preparation notification to medical staff for campaign ID: {}",
                    campaign.getCampaignId());
        } catch (Exception e) {
            log.error("Failed to send preparation notification to medical staff for campaign ID: {}. Error: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    // Notify medical staff about campaign start
    private void notifyMedicalStaffCampaignStarted(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã bắt đầu. Vui lòng thực hiện tiêm chủng theo kế hoạch.",
                campaign.getCampaignName());

        String link = "/vaccination/campaigns/" + campaign.getCampaignId();

        try {
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            log.info("Sent campaign start notification to medical staff for campaign ID: {}",
                    campaign.getCampaignId());
        } catch (Exception e) {
            log.error("Failed to send campaign start notification to medical staff for campaign ID: {}. Error: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    // Notify about campaign completion
    private void notifyAboutCampaignCompletion(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã hoàn thành.",
                campaign.getCampaignName());

        String link = "/vaccination/campaigns/" + campaign.getCampaignId();

        try {
            // Notify medical staff
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            // Notify managers
            notificationService.createAndSendNotificationToRole(
                    UserRole.StaffManager, content, link, currentUser.getEmail());

            log.info("Sent completion notification for campaign ID: {}", campaign.getCampaignId());
        } catch (Exception e) {
            log.error("Failed to send completion notification for campaign ID: {}. Error: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    // Notify about campaign cancellation
    private void notifyAboutCampaignCancellation(VaccinationCampaign campaign, User currentUser) {
        String content = String.format(
                "Chiến dịch tiêm chủng '%s' đã bị hủy.",
                campaign.getCampaignName());

        String link = "/vaccination/campaigns/" + campaign.getCampaignId();

        try {
            // Notify medical staff
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            // Notify managers
            notificationService.createAndSendNotificationToRole(
                    UserRole.StaffManager, content, link, currentUser.getEmail());

            // If consents have been sent, notify parents about cancellation
            List<VaccinationConsent> consents = vaccinationConsentRepository.findByCampaign(campaign);
            for (VaccinationConsent consent : consents) {
                User parent = consent.getParent();
                if (parent != null) {
                    String parentContent = String.format(
                            "Chiến dịch tiêm chủng '%s' cho học sinh %s đã bị hủy.",
                            campaign.getCampaignName(), consent.getStudent().getFullName());

                    notificationService.createAndSendNotification(
                            parent.getEmail(), parentContent, link, currentUser.getEmail());
                }
            }

            log.info("Sent cancellation notification for campaign ID: {}", campaign.getCampaignId());
        } catch (Exception e) {
            log.error("Failed to send cancellation notification for campaign ID: {}. Error: {}",
                    campaign.getCampaignId(), e.getMessage());
        }
    }

    // Generate consent forms for all eligible students in the campaign
    @Transactional
    public void generateConsentFormsForCampaign(VaccinationCampaign campaign, User currentUser) {
        log.info("Generating consent forms for campaign ID: {}", campaign.getCampaignId());

        // Get target students based on campaign criteria
        List<Student> targetStudents = findTargetStudentsForCampaign(campaign);
        List<VaccinationConsent> consentsToCreate = new ArrayList<>();

        for (Student student : targetStudents) {
            // Check if consent already exists
            boolean consentExists = vaccinationConsentRepository.findByCampaignAndStudent(campaign, student).isPresent();

            if (!consentExists) {
                VaccinationConsent consent = new VaccinationConsent();
                consent.setCampaign(campaign);
                consent.setStudent(student);
                consent.setStatus(ConsentStatus.PENDING);
                consent.setConsentFormSentAt(LocalDateTime.now());

                // Find parent for this student (assuming first parent link)
                if (!student.getParentLinks().isEmpty()) {
                    ParentStudentLink parentLink = student.getParentLinks().get(0);
                    consent.setParent(parentLink.getParent());
                }

                consentsToCreate.add(consent);
            }
        }

        if (!consentsToCreate.isEmpty()) {
            vaccinationConsentRepository.saveAll(consentsToCreate);
            log.info("Created {} consent forms for campaign ID: {}", consentsToCreate.size(), campaign.getCampaignId());
        }
    }

    // Tìm kiếm học sinh mục tiêu dựa trên khối lớp
    private List<Student> findTargetStudentsForCampaign(VaccinationCampaign campaign) {
        log.info("Finding target students for campaign ID: {}", campaign.getCampaignId());

        // Kiểm tra nếu chiến dịch có chỉ định khối lớp cụ thể
        if (campaign.getTargetClassGroup() != null) {
            ClassGroup targetGroup = campaign.getTargetClassGroup();
            log.info("Targeting class group: {}", targetGroup);
            // Lấy tất cả học sinh thuộc khối được chọn và có trạng thái ACTIVE
            return studentRepository.findByClassGroupAndStatus(targetGroup, StudentStatus.ACTIVE);
        }

        // Nếu không có tiêu chí khối lớp, trả về tất cả học sinh đang hoạt động
        log.info("No specific targeting criteria found, returning all active students");
        return studentRepository.findByStatus(StudentStatus.ACTIVE);
    }

    // Get all consents for a campaign with pagination
    @Transactional(readOnly = true)
    public Page<VaccinationConsentResponseDto> getConsentsForCampaign(Long campaignId, Pageable pageable) {
        authorizationService.getCurrentUserAndValidate();

        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        Page<VaccinationConsent> consentsPage = vaccinationConsentRepository.findByCampaign(campaign, pageable);
        return consentsPage.map(consentMapper::toDto);
    }

    // Get all consents for a campaign with pagination and filtering
    @Transactional(readOnly = true)
    public Page<VaccinationConsentResponseDto> getConsentsForCampaign(Long campaignId, Pageable pageable, String studentName, String className) {
        authorizationService.getCurrentUserAndValidate();

        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with ID: " + consentId));

        Student student = consent.getStudent();

        // If user is a parent, verify they are a parent of this student with ACTIVE status
        if (currentUser.getRole() == UserRole.Parent) {
            try {
                // Use AuthorizationService to check if the current user is an active parent of the student
                authorizationService.authorizeParentAction(currentUser, student, "xem phiếu đồng ý tiêm chủng");
            } catch (AccessDeniedException e) {
                log.warn("Parent {} attempted to access consent ID {} for student {} without proper link",
                        currentUser.getEmail(), consentId, student.getId());
                throw new AccessDeniedException("Bạn không có quyền xem phiếu đồng ý tiêm chủng này");
            }
        }

        // For staff and admin, allow access without additional checks
        log.info("User {} accessed consent ID: {} for student: {}",
                currentUser.getEmail(), consentId, student.getFullName());
        return consentMapper.toDto(consent);
    }

    // Update consent response from parent
    @Transactional
    public VaccinationConsentResponseDto updateConsentResponse(
            Long consentId, UpdateVaccinationConsentRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        VaccinationConsent consent = vaccinationConsentRepository.findById(consentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consent not found with ID: " + consentId));

        // Check if campaign is still accepting consent responses
        VaccinationCampaign campaign = consent.getCampaign();
        if (campaign.getStatus() != VaccinationCampaignStatus.SCHEDULED) {
            throw new InvalidOperationException("Campaign is no longer accepting consent responses");
        }

        // Check if consent deadline has passed
        if (LocalDate.now().isAfter(campaign.getConsentDeadline())) {
            throw new InvalidOperationException("Consent deadline has passed");
        }

        // Store old status for notification
        ConsentStatus oldStatus = consent.getStatus();

        // Update consent
        consentMapper.updateEntityFromDto(requestDto, consent);
        consent.setResponseReceivedAt(LocalDateTime.now());

        VaccinationConsent updatedConsent = vaccinationConsentRepository.save(consent);
        log.info("Staff {} updated consent ID: {} to status: {}",
                currentUser.getEmail(), consentId, updatedConsent.getStatus());

        // Send notification to medical staff about consent response
        notifyStaffAboutConsentResponse(updatedConsent, oldStatus, consent.getParent());

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
            // Notify campaign organizer
            User organizer = campaign.getOrganizedByUser();
            if (organizer != null) {
                notificationService.createAndSendNotification(
                        organizer.getEmail(), content, link, "hệ thống");
            }

            log.info("Sent consent response notification for student ID: {} to campaign organizer",
                    student.getId());
        } catch (Exception e) {
            log.error("Failed to send consent response notification for student ID: {}. Error: {}",
                    student.getId(), e.getMessage());
        }
    }

    // Verify all vaccinations are completed
    private void verifyAllVaccinationsCompleted(Long campaignId) {
        // Implementation would check if all students with approved consent
        // have been vaccinated or marked as absent/declined
        // For now, we'll just log
        log.info("Verifying vaccinations for campaign ID: {}", campaignId);
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
    public VaccinationCampaignResponseDto rescheduleCampaign(Long campaignId, RescheduleCampaignRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        VaccinationCampaign campaign = vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination campaign not found with ID: " + campaignId));

        if (campaign.getStatus() != VaccinationCampaignStatus.PREPARING) {
            throw new InvalidOperationException("Chỉ có thể dời lịch cho chiến dịch đang ở trạng thái Chuẩn bị (PREPARING).");
        }

        LocalDate newDate = requestDto.newVaccinationDate();
        LocalDate currentDate = campaign.getVaccinationDate();

        // Kiểm tra ngày mới phải trong hôm nay hoặc tương lai
        if (newDate.isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Ngày tiêm chủng mới phải từ hôm nay trở đi.");
        }

        // Kiểm tra ngày mới phải khác ngày hiện tại
        if (newDate.equals(currentDate)) {
            throw new InvalidOperationException("Ngày tiêm chủng mới phải khác với ngày hiện tại.");
        }

        // Cập nhật ngày tiêm chủng và thông tin dời lịch
        campaign.setVaccinationDate(newDate);
        campaign.setRescheduledAt(LocalDateTime.now());
        campaign.setRescheduledByUser(currentUser);
        campaign.setUpdatedByUser(currentUser);

        VaccinationCampaign updatedCampaign = vaccinationCampaignRepository.save(campaign);
        log.info("User {} rescheduled vaccination campaign {} from {} to {}",
                currentUser.getEmail(), campaignId, currentDate, newDate);

        // Gửi thông báo cho phụ huynh về việc thay đổi lịch
        notifyParentsAboutReschedule(updatedCampaign, currentDate, requestDto.reason());

        return enrichCampaignWithFullStatistics(updatedCampaign);
    }

    // Notify parents about campaign reschedule
    private void notifyParentsAboutReschedule(VaccinationCampaign campaign, LocalDate oldDate, String reason) {
        List<VaccinationConsent> consents = vaccinationConsentRepository.findByCampaign(campaign);

        for (VaccinationConsent consent : consents) {
            User parent = consent.getParent();
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

                    log.info("Sent reschedule notification for student ID: {} to parent: {}",
                            consent.getStudent().getId(), parent.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send reschedule notification to parent ID: {}, Email: {}. Error: {}",
                            parent.getUserId(), parent.getEmail(), e.getMessage());
                }
            }
        }
    }
}
