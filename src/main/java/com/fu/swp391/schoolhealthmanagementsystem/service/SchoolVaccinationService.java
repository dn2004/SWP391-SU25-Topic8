package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.vaccination.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ConsentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SchoolVaccinationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.VaccinationCampaignStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.PostVaccinationMonitoringMapper;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.SchoolVaccinationMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.SchoolVaccinationSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolVaccinationService {

    private final VaccinationConsentRepository vaccinationConsentRepository;
    private final SchoolVaccinationRepository schoolVaccinationRepository;
    private final PostVaccinationMonitoringRepository postVaccinationMonitoringRepository;
    private final VaccinationCampaignRepository vaccinationCampaignRepository;
    private final SchoolVaccinationMapper schoolVaccinationMapper;
    private final PostVaccinationMonitoringMapper monitoringMapper;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;
    private final SchoolVaccinationSpecification schoolVaccinationSpecification;

    // Ghi nhận một mũi tiêm chủng (hoàn thành, vắng mặt, từ chối)
    @Transactional
    public SchoolVaccinationResponseDto recordVaccination(RecordVaccinationRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        VaccinationConsent consent = vaccinationConsentRepository.findById(requestDto.consentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chấp thuận với ID: " + requestDto.consentId()));

        VaccinationCampaign campaign = consent.getCampaign();
        if (campaign.getStatus() != VaccinationCampaignStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Chỉ có thể ghi nhận tiêm chủng cho chiến dịch đang diễn ra");
        }

        // Kiểm tra xem việc tiêm chủng đã được ghi nhận cho chấp thuận này chưa
        schoolVaccinationRepository.findByConsent(consent).ifPresent(v -> {
            throw new InvalidOperationException("Tiêm chủng đã được ghi nhận cho chấp thuận có ID: " + consent.getConsentId());
        });

        // Nếu trạng thái chấp thuận không phải là APPROVED và đang cố gắng đánh dấu là COMPLETED, báo lỗi
        if (consent.getStatus() != ConsentStatus.APPROVED && requestDto.status() == SchoolVaccinationStatus.COMPLETED) {
            throw new InvalidOperationException("Không thể đánh dấu là hoàn thành khi chưa có chấp thuận được phê duyệt");
        }

        SchoolVaccination vaccination = new SchoolVaccination();
        vaccination.setCampaign(campaign);
        vaccination.setStudent(consent.getStudent());
        vaccination.setConsent(consent);
        vaccination.setStatus(requestDto.status());
        vaccination.setVaccinationDate(LocalDate.now());
        vaccination.setNotes(requestDto.notes());
        vaccination.setAdministeredByUser(currentUser);

        SchoolVaccination savedVaccination = schoolVaccinationRepository.save(vaccination);
        log.info("Người dùng {} đã ghi nhận tiêm chủng ID: {} với trạng thái: {} cho học sinh ID: {}",
                currentUser.getEmail(), savedVaccination.getSchoolVaccinationId(),
                savedVaccination.getStatus(), consent.getStudent().getId());

        // Thông báo cho phụ huynh về trạng thái tiêm chủng
        notifyParentAboutVaccinationStatus(savedVaccination, currentUser);

        // Nếu trạng thái là COMPLETED, chuyển sang POST_MONITORING
        if (requestDto.status() == SchoolVaccinationStatus.COMPLETED) {
            savedVaccination.setStatus(SchoolVaccinationStatus.POST_MONITORING);
            savedVaccination = schoolVaccinationRepository.save(savedVaccination);

            // Thông báo cho phụ huynh về việc hoàn thành tiêm chủng và bắt đầu theo dõi
            notifyParentAboutPostMonitoring(savedVaccination);

            // Thông báo cho nhân viên y tế về yêu cầu theo dõi
            notifyMedicalStaffAboutMonitoring(savedVaccination, currentUser);
        }

        return schoolVaccinationMapper.toDto(savedVaccination);
    }

    // Notify parents about vaccination status (completed, absent, or declined)
    private void notifyParentAboutVaccinationStatus(SchoolVaccination vaccination, User currentUser) {
        Student student = vaccination.getStudent();
        if (student == null || student.getParentLinks().isEmpty()) {
            log.warn("Không thể gửi thông báo về trạng thái tiêm chủng. Không có phụ huynh được liên kết với học sinh ID: {}",
                    student != null ? student.getId() : "null");
            return;
        }

        String content = getVaccineStatusString(vaccination, student);

        String link = "/vaccination/record/" + vaccination.getSchoolVaccinationId();

        student.getParentLinks().forEach(parentLink -> {
            User parent = parentLink.getParent();
            if (parent != null) {
                try {
                    notificationService.createAndSendNotification(
                            parent.getEmail(), content, link, currentUser.getEmail());

                    log.info("Đã gửi thông báo trạng thái tiêm chủng cho học sinh ID: {} đến phụ huynh: {}",
                            student.getId(), parent.getEmail());
                } catch (Exception e) {
                    log.error("Không thể gửi thông báo trạng thái tiêm chủng đến phụ huynh ID: {}, Email: {}. Lỗi: {}",
                            parent.getUserId(), parent.getEmail(), e.getMessage());
                }
            }
        });
    }

    private static String getVaccineStatusString(SchoolVaccination vaccination, Student student) {
        String statusMessage = switch (vaccination.getStatus()) {
            case COMPLETED -> "đã được tiêm";
            case ABSENT -> "vắng mặt trong buổi tiêm";
            case DECLINED -> "không được tiêm (từ chối tại chỗ)";
            default -> "có trạng thái tiêm chủng mới: " + vaccination.getStatus().getDisplayName();
        };

        return String.format(
                "Học sinh %s %s vaccine %s. %s",
                student.getFullName(), statusMessage, vaccination.getCampaign().getVaccineName(),
                vaccination.getNotes() != null && !vaccination.getNotes().isEmpty() ? "Ghi chú: " + vaccination.getNotes() : "");
    }

    // Notify parents about post-vaccination monitoring
    private void notifyParentAboutPostMonitoring(SchoolVaccination vaccination) {
        Student student = vaccination.getStudent();
        if (student == null || student.getParentLinks().isEmpty()) {
            log.warn("Không thể gửi thông báo theo dõi sau tiêm. Không có phụ huynh được liên kết với học sinh ID: {}",
                    student != null ? student.getId() : "null");
            return;
        }

        String content = String.format(
                "Học sinh %s đã được tiêm vaccine %s và đang được theo dõi sau tiêm. Nhân viên y tế sẽ thông báo nếu có bất kỳ phản ứng bất thường.",
                student.getFullName(), vaccination.getCampaign().getVaccineName());

        String link = "/vaccination/record/" + vaccination.getSchoolVaccinationId();

        student.getParentLinks().forEach(parentLink -> {
            User parent = parentLink.getParent();
            if (parent != null) {
                try {
                    notificationService.createAndSendNotification(
                            parent.getEmail(), content, link, vaccination.getAdministeredByUser().getEmail());

                    log.info("Đã gửi thông báo theo dõi sau tiêm cho học sinh ID: {} đến phụ huynh: {}",
                            student.getId(), parent.getEmail());
                } catch (Exception e) {
                    log.error("Không thể gửi thông báo theo dõi sau tiêm đến phụ huynh ID: {}, Email: {}. Lỗi: {}",
                            parent.getUserId(), parent.getEmail(), e.getMessage());
                }
            }
        });
    }

    // Notify medical staff about monitoring requirements
    private void notifyMedicalStaffAboutMonitoring(SchoolVaccination vaccination, User currentUser) {
        String content = String.format(
                "Học sinh %s vừa được tiêm vaccine %s. Cần theo dõi sau tiêm trong 30 phút.",
                vaccination.getStudent().getFullName(), vaccination.getCampaign().getVaccineName());

        String link = "/vaccination/record/" + vaccination.getSchoolVaccinationId() + "/monitoring";

        try {
            // Notify current user as a reminder to monitor
            if (currentUser.getRole() == UserRole.MedicalStaff) {
                notificationService.createAndSendNotification(
                        currentUser.getEmail(), content, link, "system");
            }

            // Notify other medical staff on duty
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            log.info("Đã gửi nhắc nhở theo dõi cho tiêm chủng ID: {}", vaccination.getSchoolVaccinationId());
        } catch (Exception e) {
            log.error("Không thể gửi nhắc nhở theo dõi cho tiêm chủng ID: {}. Lỗi: {}",
                    vaccination.getSchoolVaccinationId(), e.getMessage());
        }
    }

    // Record post-vaccination monitoring
    @Transactional
    public PostVaccinationMonitoringResponseDto recordPostVaccinationMonitoring(
            CreatePostVaccinationMonitoringRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        SchoolVaccination vaccination = schoolVaccinationRepository.findById(requestDto.schoolVaccinationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "School vaccination not found with ID: " + requestDto.schoolVaccinationId()));

        // Verify the vaccination is in POST_MONITORING status
        if (vaccination.getStatus() != SchoolVaccinationStatus.POST_MONITORING) {
            throw new InvalidOperationException(
                    "Cannot record monitoring for vaccination not in POST_MONITORING status");
        }

        // Check if monitoring record already exists (since it's 1-1 relationship)
        Optional<PostVaccinationMonitoring> existingMonitoring =
                postVaccinationMonitoringRepository.findBySchoolVaccination(vaccination);

        if (existingMonitoring.isPresent()) {
            throw new InvalidOperationException(
                    "Monitoring record already exists for this vaccination. Use update instead.");
        }

        PostVaccinationMonitoring monitoring = monitoringMapper.toEntity(requestDto);
        monitoring.setSchoolVaccination(vaccination);
        monitoring.setMonitoringTime(LocalDateTime.now());
        monitoring.setRecordedByUser(currentUser);

        PostVaccinationMonitoring savedMonitoring = postVaccinationMonitoringRepository.save(monitoring);
        log.info("Người dùng {} đã ghi nhận theo dõi sau tiêm ID: {} cho tiêm chủng ID: {}",
                currentUser.getEmail(), savedMonitoring.getMonitoringId(), vaccination.getSchoolVaccinationId());

        // If side effects reported, notify parents and school management
        if (Boolean.TRUE.equals(savedMonitoring.getHasSideEffects())) {
            notifyAboutSideEffects(savedMonitoring, vaccination, currentUser);
        }

        return monitoringMapper.toDto(savedMonitoring);
    }

    // Notify about side effects
    private void notifyAboutSideEffects(PostVaccinationMonitoring monitoring, SchoolVaccination vaccination, User currentUser) {
        Student student = vaccination.getStudent();
        if (student == null) {
            log.warn("Không thể gửi thông báo về phản ứng phụ. Không có thông tin học sinh cho tiêm chủng ID: {}",
                    vaccination.getSchoolVaccinationId());
            return;
        }

        String content = String.format(
                "Phát hiện phản ứng sau tiêm ở học sinh %s: %s. Nhiệt độ: %.1f°C. %s",
                student.getFullName(),
                monitoring.getSideEffectsDescription(),
                monitoring.getTemperature() != null ? monitoring.getTemperature() : 0.0f,
                monitoring.getActionsTaken() != null ? "Đã xử lý: " + monitoring.getActionsTaken() : "");

        String link = "/vaccination/monitoring/" + monitoring.getMonitoringId();

        try {
            // Notify school managers
            notificationService.createAndSendNotificationToRole(
                    UserRole.StaffManager, content, link, currentUser.getEmail());

            // Notify other medical staff
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            // Notify parents
            notifyParentsAboutSideEffects(student, content, link, currentUser.getEmail());

            log.info("Đã gửi thông báo về phản ứng phụ cho học sinh ID: {}, theo dõi ID: {}",
                    student.getId(), monitoring.getMonitoringId());
        } catch (Exception e) {
            log.error("Không thể gửi thông báo về phản ứng phụ cho theo dõi ID: {}. Lỗi: {}",
                    monitoring.getMonitoringId(), e.getMessage());
        }
    }

    // Notify parents about side effects
    private void notifyParentsAboutSideEffects(Student student, String content, String link, String senderEmail) {
        if (student.getParentLinks().isEmpty()) {
            log.warn("Không thể gửi thông báo về phản ứng phụ đến phụ huynh. Không có phụ huynh được liên kết với học sinh ID: {}",
                    student.getId());
            return;
        }

        student.getParentLinks().forEach(parentLink -> {
            User parent = parentLink.getParent();
            if (parent != null) {
                try {
                    notificationService.createAndSendNotification(
                            parent.getEmail(), content, link, senderEmail);
                } catch (Exception e) {
                    log.error("Không thể gửi thông báo về phản ứng phụ đến phụ huynh ID: {}, Email: {}. Lỗi: {}",
                            parent.getUserId(), parent.getEmail(), e.getMessage());
                }
            }
        });
    }

    /**
     * Cập nhật trạng thái record vaccination (chỉ cho phép khi campaign đang IN_PROGRESS)
     */
    @Transactional
    public SchoolVaccinationResponseDto updateVaccinationRecord(Long vaccinationId, UpdateVaccinationRecordRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        SchoolVaccination vaccination = schoolVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination record not found with ID: " + vaccinationId));

        VaccinationCampaign campaign = vaccination.getCampaign();

        // Kiểm tra campaign chưa hoàn thành
        if (campaign.getStatus() == VaccinationCampaignStatus.COMPLETED) {
            throw new InvalidOperationException("Không thể cập nhật record khi chiến dịch đã hoàn thành");
        }

        // Kiểm tra campaign phải đang IN_PROGRESS
        if (campaign.getStatus() != VaccinationCampaignStatus.IN_PROGRESS) {
            throw new InvalidOperationException("Chỉ có thể cập nhật record khi chiến dịch đang diễn ra (IN_PROGRESS)");
        }

        SchoolVaccinationStatus oldStatus = vaccination.getStatus();
        SchoolVaccinationStatus newStatus = requestDto.status();

        // Kiểm tra không cho phép thay đổi từ POST_MONITORING về các trạng thái khác
        if (oldStatus == SchoolVaccinationStatus.POST_MONITORING && newStatus != SchoolVaccinationStatus.COMPLETED) {
            throw new InvalidOperationException("Không thể thay đổi từ trạng thái THEO DÕI sang trạng thái khác (trừ COMPLETED)");
        }

        // Kiểm tra consent status khi cập nhật sang COMPLETED
        VaccinationConsent consent = vaccination.getConsent();
        if (newStatus == SchoolVaccinationStatus.COMPLETED && consent.getStatus() != ConsentStatus.APPROVED) {
            throw new InvalidOperationException("Không thể đánh dấu COMPLETED khi chưa có chấp thuận được phê duyệt");
        }

        // Cập nhật thông tin
        vaccination.setStatus(newStatus);
        vaccination.setNotes(requestDto.notes());
        vaccination.setUpdatedByUser(currentUser);

        SchoolVaccination updatedVaccination = schoolVaccinationRepository.save(vaccination);

        log.info("User {} updated vaccination record ID: {} from {} to {}. Reason: {}",
                currentUser.getEmail(), vaccinationId, oldStatus, newStatus, requestDto.reasonForChange());

        // Gửi thông báo về thay đổi trạng thái
        notifyParentAboutStatusUpdate(updatedVaccination, oldStatus, newStatus, requestDto.reasonForChange(), currentUser);

        // Xử lý logic đặc biệt khi chuyển sang COMPLETED
        if (newStatus == SchoolVaccinationStatus.COMPLETED && oldStatus != SchoolVaccinationStatus.POST_MONITORING) {
            updatedVaccination.setStatus(SchoolVaccinationStatus.POST_MONITORING);
            updatedVaccination = schoolVaccinationRepository.save(updatedVaccination);

            // Thông báo về việc bắt đầu theo dõi
            notifyParentAboutPostMonitoring(updatedVaccination);
            notifyMedicalStaffAboutMonitoring(updatedVaccination, currentUser);
        }

        return schoolVaccinationMapper.toDto(updatedVaccination);
    }

    // Notify parents about status update
    private void notifyParentAboutStatusUpdate(SchoolVaccination vaccination, SchoolVaccinationStatus oldStatus,
                                             SchoolVaccinationStatus newStatus, String reason, User currentUser) {
        Student student = vaccination.getStudent();
        if (student == null || student.getParentLinks().isEmpty()) {
            return;
        }

        String statusMessage = switch (newStatus) {
            case COMPLETED -> "đã được tiêm";
            case ABSENT -> "vắng mặt trong buổi tiêm";
            case DECLINED -> "không được tiêm (từ chối tại chỗ)";
            default -> "có trạng thái mới: " + newStatus.getDisplayName();
        };

        String content = String.format(
                "Cập nhật: Trạng thái tiêm chủng của học sinh %s đã được thay đổi. %s vaccine %s. %s%s",
                student.getFullName(),
                student.getFullName(),
                statusMessage,
                vaccination.getCampaign().getVaccineName(),
                reason != null && !reason.trim().isEmpty() ? " Lý do: " + reason : "",
                vaccination.getNotes() != null && !vaccination.getNotes().isEmpty() ? " Ghi chú: " + vaccination.getNotes() : "");

        String link = "/vaccination/record/" + vaccination.getSchoolVaccinationId();

        student.getParentLinks().forEach(parentLink -> {
            User parent = parentLink.getParent();
            if (parent != null) {
                try {
                    notificationService.createAndSendNotification(
                            parent.getEmail(), content, link, currentUser.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send status update notification to parent: {}", e.getMessage());
                }
            }
        });
    }

    // Get vaccinations for a campaign with pagination and filtering
    @Transactional(readOnly = true)
    public Page<SchoolVaccinationResponseDto> getVaccinationsForCampaign(
            Long campaignId,
            String studentName,
            String className,
            SchoolVaccinationStatus status,
            Pageable pageable) {

        authorizationService.getCurrentUserAndValidate();

        vaccinationCampaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found with ID: " + campaignId));

        Specification<SchoolVaccination> spec = Specification
                .allOf(
                    schoolVaccinationSpecification.forCampaign(campaignId),
                    schoolVaccinationSpecification.forStudentName(studentName),
                    schoolVaccinationSpecification.forStudentClass(className),
                    schoolVaccinationSpecification.hasStatus(status)
                );

        Page<SchoolVaccination> vaccinationsPage = schoolVaccinationRepository.findAll(spec, pageable);
        return vaccinationsPage.map(schoolVaccinationMapper::toDto);
    }

    // Get a vaccination by ID
    @Transactional(readOnly = true)
    public SchoolVaccinationResponseDto getVaccinationById(Long vaccinationId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        SchoolVaccination vaccination = schoolVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination not found with ID: " + vaccinationId));

        return schoolVaccinationMapper.toDto(vaccination);
    }

    // Get post-vaccination monitoring record for a vaccination
    @Transactional(readOnly = true)
    public PostVaccinationMonitoringResponseDto getMonitoringForVaccination(Long vaccinationId) {
        authorizationService.getCurrentUserAndValidate();

        SchoolVaccination vaccination = schoolVaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination not found with ID: " + vaccinationId));

        Optional<PostVaccinationMonitoring> monitoringRecord =
                postVaccinationMonitoringRepository.findBySchoolVaccination(vaccination);

        if (monitoringRecord.isEmpty()) {
            throw new ResourceNotFoundException("No monitoring record found for vaccination ID: " + vaccinationId);
        }

        return monitoringMapper.toDto(monitoringRecord.get());
    }

    /**
     * Cập nhật bản ghi theo dõi sau tiêm chủng
     */
    @Transactional
    public PostVaccinationMonitoringResponseDto updatePostVaccinationMonitoring(
            Long monitoringId, UpdatePostVaccinationMonitoringRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();

        PostVaccinationMonitoring monitoring = postVaccinationMonitoringRepository.findById(monitoringId)
                .orElseThrow(() -> new ResourceNotFoundException("Monitoring record not found with ID: " + monitoringId));

        SchoolVaccination vaccination = monitoring.getSchoolVaccination();
        VaccinationCampaign campaign = vaccination.getCampaign();

        // Kiểm tra campaign chưa hoàn thành
        if (campaign.getStatus() == VaccinationCampaignStatus.COMPLETED) {
            throw new InvalidOperationException("Không thể cập nhật bản ghi theo dõi khi chiến dịch đã hoàn thành");
        }

        // Kiểm tra vaccination phải đang POST_MONITORING
        if (vaccination.getStatus() != SchoolVaccinationStatus.POST_MONITORING) {
            throw new InvalidOperationException("Chỉ có thể cập nhật bản ghi theo dõi khi vaccination đang ở trạng thái POST_MONITORING");
        }

        // Lưu thông tin cũ để so sánh
        Boolean oldHasSideEffects = monitoring.getHasSideEffects();
        Float oldTemperature = monitoring.getTemperature();

        // Cập nhật thông tin
        if (requestDto.temperature() != null) {
            monitoring.setTemperature(requestDto.temperature());
        }
        if (requestDto.hasSideEffects() != null) {
            monitoring.setHasSideEffects(requestDto.hasSideEffects());
        }
        if (requestDto.sideEffectsDescription() != null) {
            monitoring.setSideEffectsDescription(requestDto.sideEffectsDescription());
        }
        if (requestDto.actionsTaken() != null) {
            monitoring.setActionsTaken(requestDto.actionsTaken());
        }
        if (requestDto.notes() != null) {
            monitoring.setNotes(requestDto.notes());
        }

        PostVaccinationMonitoring updatedMonitoring = postVaccinationMonitoringRepository.save(monitoring);

        log.info("User {} updated monitoring record ID: {}. Reason: {}",
                currentUser.getEmail(), monitoringId, requestDto.reasonForUpdate());

        // Nếu có thay đổi quan trọng về phản ứng phụ, gửi thông báo
        if (hasSignificantChange(oldHasSideEffects, oldTemperature, updatedMonitoring)) {
            notifyAboutMonitoringUpdate(updatedMonitoring, vaccination, requestDto.reasonForUpdate(), currentUser);
        }

        return monitoringMapper.toDto(updatedMonitoring);
    }

    // Kiểm tra có thay đổi quan trọng trong theo dõi không
    private boolean hasSignificantChange(Boolean oldHasSideEffects, Float oldTemperature, PostVaccinationMonitoring newMonitoring) {
        // Thay đổi từ không có phản ứng phụ sang có phản ứng phụ
        if (!Boolean.TRUE.equals(oldHasSideEffects) && Boolean.TRUE.equals(newMonitoring.getHasSideEffects())) {
            return true;
        }

        // Thay đổi nhiệt độ đáng kể (> 1°C)
        if (oldTemperature != null && newMonitoring.getTemperature() != null) {
            float tempDiff = Math.abs(newMonitoring.getTemperature() - oldTemperature);
            if (tempDiff > 1.0f) {
                return true;
            }
        }

        return false;
    }

    // Thông báo về cập nhật theo dõi
    private void notifyAboutMonitoringUpdate(PostVaccinationMonitoring monitoring, SchoolVaccination vaccination,
                                           String reason, User currentUser) {
        Student student = vaccination.getStudent();
        if (student == null) {
            return;
        }

        String content = String.format(
                "Cập nhật theo dõi sau tiêm cho học sinh %s: Nhiệt độ %.1f°C, %s. %s",
                student.getFullName(),
                monitoring.getTemperature() != null ? monitoring.getTemperature() : 0.0f,
                Boolean.TRUE.equals(monitoring.getHasSideEffects())
                    ? "Có phản ứng phụ: " + monitoring.getSideEffectsDescription()
                    : "Không có phản ứng phụ",
                reason != null && !reason.trim().isEmpty() ? "Lý do cập nhật: " + reason : "");

        String link = "/vaccination/monitoring/" + monitoring.getMonitoringId();

        try {
            // Notify medical staff
            notificationService.createAndSendNotificationToRole(
                    UserRole.MedicalStaff, content, link, currentUser.getEmail());

            // Notify parents if there are significant changes
            if (Boolean.TRUE.equals(monitoring.getHasSideEffects())) {
                student.getParentLinks().forEach(parentLink -> {
                    User parent = parentLink.getParent();
                    if (parent != null) {
                        try {
                            notificationService.createAndSendNotification(
                                    parent.getEmail(), content, link, currentUser.getEmail());
                        } catch (Exception e) {
                            log.error("Failed to send monitoring update notification to parent: {}", e.getMessage());
                        }
                    }
                });
            }

            log.info("Sent monitoring update notification for student ID: {}, monitoring ID: {}",
                    student.getId(), monitoring.getMonitoringId());
        } catch (Exception e) {
            log.error("Failed to send monitoring update notification for monitoring ID: {}. Error: {}",
                    monitoring.getMonitoringId(), e.getMessage());
        }
    }
}
