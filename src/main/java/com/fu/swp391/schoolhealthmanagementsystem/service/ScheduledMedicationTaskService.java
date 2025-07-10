package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.AdministerMedicationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.ScheduledMedicationTaskResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.medication.SkipMedicationTaskRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.*;
import com.fu.swp391.schoolhealthmanagementsystem.exception.FileStorageException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.ScheduledMedicationTaskMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.ScheduledMedicationTaskSpecification;
import com.fu.swp391.schoolhealthmanagementsystem.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledMedicationTaskService {

    private final ScheduledMedicationTaskRepository taskRepository;
    private final StudentMedicationRepository studentMedicationRepository;
    private final StudentRepository studentRepository;
    private final ScheduledMedicationTaskMapper taskMapper;
    private final AuthorizationService authorizationService;
    private final FileStorageService fileStorageService;
    private final StudentMedicationTransactionRepository studentMedicationTransactionRepository;
    private final UserRepository userRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final ScheduledMedicationTaskSpecification scheduledMedicationTaskSpecification;
    private final NotificationService notificationService;
    // ParentStudentLinkRepository sẽ được gọi qua AuthorizationService

    /**
     * Lấy danh sách (phân trang) các ScheduledMedicationTask cho một StudentMedication cụ thể.
     * Áp dụng phân quyền.
     */
    @Transactional(readOnly = true)
    public Page<ScheduledMedicationTaskResponseDto> getTasksByStudentMedicationId(Long studentMedicationId, Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} (Role: {}) is requesting scheduled tasks for StudentMedication ID: {}",
                currentUser.getEmail(), currentUser.getRole(), studentMedicationId);

        // Bước 1: Kiểm tra StudentMedication có tồn tại không
        StudentMedication studentMedication = studentMedicationRepository.findById(studentMedicationId)
                .orElseThrow(() -> {
                    log.warn("StudentMedication not found with ID: {}", studentMedicationId);
                    return new ResourceNotFoundException("Không tìm thấy thông tin thuốc của học sinh với ID: " + studentMedicationId);
                });

        // Bước 2: Phân quyền
        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.Parent) {
            Student studentOfMedication = studentMedication.getStudent();
            if (studentOfMedication == null) {
                log.error("Data integrity issue: StudentMedication ID {} has no associated Student.", studentMedicationId);
                throw new IllegalStateException("Dữ liệu thuốc không hợp lệ, không tìm thấy thông tin học sinh liên quan.");
            }
            // Sử dụng hàm authorizeParentAction để kiểm tra quyền của phụ huynh đối với học sinh này
            try {
                authorizationService.authorizeParentAction(currentUser, studentOfMedication, "xem lịch uống thuốc của con");
            } catch (AccessDeniedException e) {
                log.warn("Parent {} (ID: {}) is not authorized to view medication schedule for student {} (ID: {}). Medication ID: {}",
                        currentUser.getEmail(), currentUser.getUserId(),
                        studentOfMedication.getFullName(), studentOfMedication.getId(),
                        studentMedicationId);
                throw e;
            }
        } else if (!(currentUserRole == UserRole.MedicalStaff ||
                currentUserRole == UserRole.StaffManager ||
                currentUserRole == UserRole.SchoolAdmin)) {
            log.warn("User {} (Role: {}) is not authorized to view medication schedule for StudentMedication ID: {}",
                    currentUser.getEmail(), currentUserRole, studentMedicationId);
            throw new AccessDeniedException("Bạn không có quyền xem lịch uống thuốc này.");
        }

        // Bước 3: Lấy dữ liệu task
        Page<ScheduledMedicationTask> tasksPage = taskRepository.findByStudentMedication_StudentMedicationId(studentMedicationId, pageable);
        log.info("Found {} scheduled tasks for StudentMedication ID {} on page {}",
                tasksPage.getNumberOfElements(), studentMedicationId, pageable.getPageNumber());

        return tasksPage.map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public String getTaskProofAccessUrl(Long taskId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} is requesting proof access URL for ScheduledTask ID: {}", currentUser.getEmail(), taskId);

        ScheduledMedicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch uống thuốc với ID: " + taskId));

        // Authorization Check
        StudentMedication studentMedication = task.getStudentMedication();
        if (studentMedication == null) {
            throw new IllegalStateException("Task with ID " + taskId + " is not linked to any medication.");
        }
        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, studentMedication.getStudent(), "xem bằng chứng uống thuốc của con");
        } else if (!(currentUserRole == UserRole.MedicalStaff ||
                currentUserRole == UserRole.StaffManager ||
                currentUserRole == UserRole.SchoolAdmin)) {
            throw new AccessDeniedException("Bạn không có quyền xem bằng chứng uống thuốc này.");
        }

        // Check if proof exists
        if (task.getProofPublicId() == null || task.getProofPublicId().isBlank()) {
            log.warn("Task ID {} does not have a proof file.", taskId);
            throw new ResourceNotFoundException("Không có file bằng chứng nào được tìm thấy cho công việc này.");
        }

        // Generate URL using FileStorageService
        try {
            return fileStorageService.generateSignedUrl(task.getProofPublicId(), task.getProofResourceType(), 180);
        } catch (Exception e) {
            log.error("Failed to generate access URL for task ID {}: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Không thể tạo URL truy cập cho file bằng chứng.");
        }
    }

    @Transactional
    public ScheduledMedicationTaskResponseDto administerMedicationTask(Long taskId, AdministerMedicationRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        log.info("Staff {} is administering medication for ScheduledTask ID: {}", currentStaff.getEmail(), taskId);

        ScheduledMedicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch uống thuốc với ID: " + taskId));

        if (task.getStatus() != ScheduledMedicationTaskStatus.SCHEDULED) {
            throw new IllegalStateException("Chỉ có thể xác nhận cho uống đối với lịch đang ở trạng thái 'Đã lên lịch'. Trạng thái hiện tại: " + task.getStatus().getDisplayName());
        }

        StudentMedication medication = task.getStudentMedication();
        if (medication == null) {
            throw new IllegalStateException("Lịch uống thuốc ID " + taskId + " không liên kết với thông tin thuốc nào.");
        }

        // Nếu actualDosesAdministered > dosesToAdministerInTask, cần có quy trình nghiệp vụ rõ ràng (có được phép không?)
        // Hiện tại, giả sử actualDosesAdministered <= dosesToAdministerInTask hoặc bằng.

        // Cập nhật ScheduledMedicationTask
        task.setStatus(ScheduledMedicationTaskStatus.ADMINISTERED);
        task.setAdministeredAt(LocalDateTime.of(LocalDate.now(), requestDto.administeredTime()));
        task.setStaffNotes(requestDto.staffNotes());
        task.setAdministeredByStaff(currentStaff);

        // Xử lý upload file bằng chứng
        MultipartFile proofFile = requestDto.proofFile();
        if (proofFile != null && !proofFile.isEmpty()) {
            // Xóa file cũ nếu có (nếu cho phép cập nhật bằng chứng) - logic này có thể không cần nếu chỉ upload 1 lần
            // if (task.getProofPublicId() != null) { ... fileStorageService.deleteFile ... }

            String folderName = "medication_proofs/task_" + task.getScheduledTaskId();
            String publicIdPrefix = "task_" + task.getScheduledTaskId() + "_proof";
            try {
                CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(proofFile, folderName, publicIdPrefix);
                task.setProofFileOriginalName(proofFile.getOriginalFilename());
                task.setProofFileType(proofFile.getContentType());
                task.setProofPublicId(uploadResult.publicId()); // Giả sử CloudinaryUploadResponse có publicId()
                task.setProofResourceType(uploadResult.resourceType()); // và resourceType()
            } catch (Exception e) {
                log.error("Failed to upload proof file for task ID {}: {}", taskId, e.getMessage(), e);
                // Quyết định: có ném lỗi và rollback không, hay vẫn tiếp tục và chỉ log lỗi upload?
                // Hiện tại, sẽ ném lỗi nếu upload file là bắt buộc.
                throw new FileStorageException("Lỗi khi tải lên file bằng chứng: " + e.getMessage(), e);
            }
        }

        ScheduledMedicationTask updatedTask = taskRepository.save(task);

        // Cập nhật StudentMedication (giảm số liều)
        int oldRemainingDoses = medication.getRemainingDoses();
        int newRemainingDoses = oldRemainingDoses - 1;
        log.info("newRemainingDoses: {}, oldRemainingDoses: {}",
                newRemainingDoses, oldRemainingDoses);
        medication.setRemainingDoses(newRemainingDoses);
        medication.setUpdatedByUser(currentStaff);
        if (newRemainingDoses <= 0) {
            medication.setStatus(MedicationStatus.OUT_OF_DOSES);
            // Nếu đã hết liều, có thể set isScheduleActive = false (nếu còn dùng trường đó)
            // medication.setScheduleActive(false);
            log.info("Medication ID {} is now OUT_OF_DOSES.", medication.getStudentMedicationId());
        }
        studentMedicationRepository.save(medication);
        // Tạo StudentMedicationTransaction
        StudentMedicationTransaction transaction = StudentMedicationTransaction.builder()
                .studentMedication(medication)
                .transactionType(StudentMedicationTransactionType.ADMINISTERED)
                .performedByUser(currentStaff)
                .scheduledMedicationTask(updatedTask)
                .notes("Đã cho học sinh uống thuốc theo lịch. Task ID: " + updatedTask.getScheduledTaskId() +
                        (requestDto.staffNotes() != null && !requestDto.staffNotes().isBlank() ? ". Ghi chú NVYT: " + requestDto.staffNotes() : ""))
                .build();
        studentMedicationTransactionRepository.save(transaction);

        log.info("Medication administered successfully for ScheduledTask ID {} by staff {}. Remaining doses for {} : {}",
                taskId, currentStaff.getEmail(), medication.getMedicationName(), newRemainingDoses);

        // Send notifications
        sendMedicationAdministeredNotification(updatedTask);
        checkAndSendLowDoseNotification(medication);

        return taskMapper.toDto(updatedTask);
    }

    @Transactional
    public ScheduledMedicationTaskResponseDto skipMedicationTask(Long taskId, SkipMedicationTaskRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        log.info("Staff {} is skipping ScheduledTask ID: {} with reason: {}. Notes: {}",
                currentStaff.getEmail(), taskId, requestDto.skipReasonStatus(), requestDto.staffNotes());

        ScheduledMedicationTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch uống thuốc với ID: " + taskId));

        if (task.getStatus() != ScheduledMedicationTaskStatus.SCHEDULED) {
            throw new IllegalStateException("Chỉ có thể bỏ qua đối với lịch đang ở trạng thái 'Đã lên lịch'. Trạng thái hiện tại: " + task.getStatus().getDisplayName());
        }

        // Cập nhật thông tin task bị bỏ qua
        task.setStatus(requestDto.skipReasonStatus());
        task.setStaffNotes(requestDto.staffNotes());
        task.setAdministeredByStaff(currentStaff); // Ghi nhận NVYT xử lý việc skip này
        task.setAdministeredAt(LocalDateTime.now()); // Thời điểm ghi nhận việc skip

        ScheduledMedicationTask updatedTask = taskRepository.save(task);
        log.info("ScheduledTask ID {} status updated to {} by staff {}", taskId, updatedTask.getStatus(), currentStaff.getEmail());

        StudentMedication medication = updatedTask.getStudentMedication();

        // Xử lý logic tiếp theo dựa trên lý do skip
        switch (requestDto.skipReasonStatus()) {
            case SKIPPED_STUDENT_ABSENT:
            case SKIPPED_STUDENT_REFUSED:
            case NOT_ADMINISTERED_OTHER: // Coi như các lý do này có thể dời lịch
                log.info("Attempting to reschedule task ID {} due to student-related skip reason: {}", taskId, requestDto.skipReasonStatus());
                tryToRescheduleTask(updatedTask, medication, currentStaff);
                break;
            case SKIPPED_SUPPLY_ISSUE:
                log.warn("Task ID {} skipped due to supply issue for Medication ID {}. Staff notes: {}",
                        taskId, medication.getStudentMedicationId(), requestDto.staffNotes());
                break;
            default:
                break;
        }

        // Send notification
        sendMedicationSkippedNotification(updatedTask);

        return taskMapper.toDto(updatedTask);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledMedicationTaskResponseDto> getHandledTasksByStaff(
            Long staffId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            ScheduledMedicationTaskStatus status,
            Pageable pageable) {

        User currentUser = authorizationService.getCurrentUserAndValidate();
        User targetStaff = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhân viên y tế với ID: " + staffId));

        // Phân quyền
        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.MedicalStaff) {
            // MedicalStaff chỉ được xem lịch sử của chính mình
            if (!currentUser.getUserId().equals(targetStaff.getUserId())) {
                log.warn("MedicalStaff {} (ID: {}) attempted to view handled tasks of another staff (ID: {}). Access denied.",
                        currentUser.getEmail(), currentUser.getUserId(), staffId);
                throw new AccessDeniedException("Bạn chỉ có quyền xem lịch sử công việc của chính mình.");
            }
        } else if (!(currentUserRole == UserRole.StaffManager || currentUserRole == UserRole.SchoolAdmin)) {
            // Các vai trò khác (ngoài những người được phép xem của người khác) không có quyền
            log.warn("User {} (Role: {}) attempted to view handled tasks of staff (ID: {}). Access denied.",
                    currentUser.getEmail(), currentUserRole, staffId);
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này.");
        }
        // StaffManager và SchoolAdmin được phép xem của bất kỳ staff nào.

        log.info("User {} is requesting handled tasks by staff {} (ID: {}) for period: {} to {}, status filter: {}",
                currentUser.getEmail(), targetStaff.getFullName(), staffId,
                startDate != null ? startDate : "N/A",
                endDate != null ? endDate : "N/A",
                status != null ? status.getDisplayName() : "All statuses");

        // Sử dụng Specification để tạo query động
        Specification<ScheduledMedicationTask> spec = Specification.allOf(
                scheduledMedicationTaskSpecification.administeredByStaff(staffId)
                        .and(scheduledMedicationTaskSpecification.isHandled()).and(scheduledMedicationTaskSpecification.hasStatus(status)).and(scheduledMedicationTaskSpecification.administeredOnOrAfter(startDate)).and(scheduledMedicationTaskSpecification.administeredOnOrBefore(endDate)));

        // Thực hiện truy vấn với specification
        Page<ScheduledMedicationTask> tasksPage = taskRepository.findAll(spec, pageable);

        log.info("Found {} handled tasks by staff {} (ID: {}), with applied filters",
                tasksPage.getTotalElements(), targetStaff.getFullName(), staffId);

        return tasksPage.map(taskMapper::toDto);
    }

    private void tryToRescheduleTask(ScheduledMedicationTask skippedTask, StudentMedication medication, User responsibleStaff) {
        LocalDate nextWorkday = DateUtils.getNextWorkday(skippedTask.getScheduledDate());

        if (taskRepository.existsByStudentMedicationAndScheduledDateAndScheduledTimeText(
                medication, nextWorkday, skippedTask.getScheduledTimeText())) {
            log.warn("Rescheduled task for Medication ID {} on {} at {} already exists. Skipping creation of duplicate.",
                    medication.getStudentMedicationId(), nextWorkday, skippedTask.getScheduledTimeText());
            return;
        }


        ScheduledMedicationTask rescheduledTask = ScheduledMedicationTask.builder()
                .studentMedication(medication)
                .scheduledDate(nextWorkday)
                .scheduledTimeText(skippedTask.getScheduledTimeText())
                .status(ScheduledMedicationTaskStatus.SCHEDULED)
                .schoolSession(skippedTask.getSchoolSession())
                .staffNotes("Dời lịch từ task ID " + skippedTask.getScheduledTaskId() +
                        " (ngày " + skippedTask.getScheduledDate() + ") do: " +
                        skippedTask.getStatus().getDisplayName() +
                        (skippedTask.getStaffNotes() != null && !skippedTask.getStaffNotes().isBlank() ? ". Ghi chú gốc: " + skippedTask.getStaffNotes() : ""))
                .requestedAt(LocalDateTime.now())
                .build();

        taskRepository.save(rescheduledTask);
        log.info("Rescheduled task for Medication ID {}. New task ID: {}, Scheduled for: {} {}",
                medication.getStudentMedicationId(), rescheduledTask.getScheduledTaskId(),
                rescheduledTask.getScheduledDate(), rescheduledTask.getScheduledTimeText());
    }

    private void sendNotificationToParents(Student student, String content, String link, String sender, String logContext) {
        if (student == null || student.getParentLinks() == null || student.getParentLinks().isEmpty()) {
            log.warn("Cannot send {} notification. No parent info for student ID: {}", logContext, student != null ? student.getId() : "null");
            return;
        }

        student.getParentLinks().forEach(parentLink -> {
            User parent = parentLink.getParent();
            if (parent != null && parent.getEmail() != null) {
                notificationService.createAndSendNotification(parent.getEmail(), content, link, sender);
                log.info("Requested to send {} notification to parent: {} from sender: {}", logContext, parent.getEmail(), sender);
            }
        });
    }

    private void sendMedicationAdministeredNotification(ScheduledMedicationTask task) {
        try {
            Student student = task.getStudentMedication().getStudent();
            String medicationName = task.getStudentMedication().getMedicationName();
            String administeredTime = task.getAdministeredAt().toLocalTime().toString();
            User staff = task.getAdministeredByStaff();
            String sender = (staff != null) ? staff.getEmail() : "system";

            String content = String.format("Học sinh %s đã được cho uống thuốc '%s' vào lúc %s.",
                    student.getFullName(), medicationName, administeredTime);
            String link = "/scheduled-medication-tasks/" + task.getScheduledTaskId();

            sendNotificationToParents(student, content, link, sender, "medication administered");
        } catch (Exception e) {
            log.error("Error sending medication administered notification for task ID {}: {}", task.getScheduledTaskId(), e.getMessage(), e);
        }
    }

    private void checkAndSendLowDoseNotification(StudentMedication medication) {
        final int LOW_DOSE_THRESHOLD = 3;
        if (medication.getRemainingDoses() > 0 && medication.getRemainingDoses() <= LOW_DOSE_THRESHOLD) {
            try {
                Student student = medication.getStudent();
                String content = String.format("Thuốc '%s' của học sinh %s sắp hết. Số liều còn lại: %d. Vui lòng gửi thêm.",
                        medication.getMedicationName(), student.getFullName(), medication.getRemainingDoses());
                String link = "/student-medications/" + medication.getStudentMedicationId();

                sendNotificationToParents(student, content, link, "system", "low dose");
            } catch (Exception e) {
                log.error("Error sending low dose notification for medication ID {}: {}", medication.getStudentMedicationId(), e.getMessage(), e);
            }
        }
    }

    private void sendMedicationSkippedNotification(ScheduledMedicationTask task) {
        try {
            Student student = task.getStudentMedication().getStudent();
            String medicationName = task.getStudentMedication().getMedicationName();
            String reason = task.getStatus().getDisplayName();
            User staff = task.getAdministeredByStaff();
            String sender = (staff != null) ? staff.getEmail() : "system";

            String content = String.format("Lịch uống thuốc '%s' của học sinh %s đã bị bỏ qua. Lý do: %s.",
                    medicationName, student.getFullName(), reason);
            String link = "/scheduled-medication-tasks/" + task.getScheduledTaskId();

            sendNotificationToParents(student, content, link, sender, "medication skipped");
        } catch (Exception e) {
            log.error("Error sending medication skipped notification for task ID {}: {}", task.getScheduledTaskId(), e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<ScheduledMedicationTaskResponseDto> getScheduledTasksForDate(LocalDate targetDate, Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // Phân quyền sẽ được xử lý ở Controller bằng @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")

        LocalDate dateToQuery = (targetDate == null) ? LocalDate.now() : targetDate;

        log.info("User {} (Role: {}) is requesting scheduled tasks for date: {}",
                currentUser.getEmail(), currentUser.getRole(), dateToQuery);

        // Sử dụng Specification để tạo query
        Specification<ScheduledMedicationTask> spec = Specification.allOf(
                        scheduledMedicationTaskSpecification.hasScheduledDate(dateToQuery))
                .and(scheduledMedicationTaskSpecification.hasStatus(ScheduledMedicationTaskStatus.SCHEDULED));

        Page<ScheduledMedicationTask> tasksPage = taskRepository.findAll(spec, pageable);

        log.info("Found {} scheduled tasks for date {} on page {}",
                tasksPage.getNumberOfElements(), dateToQuery, pageable.getPageNumber());

        return tasksPage.map(taskMapper::toDto);
    }

    /**
     * Lấy lịch sử nhiệm vụ uống thuốc của một học sinh.
     * Phụ huynh chỉ xem được lịch sử của con mình.
     * NVYT, Quản lý NVYT, Admin trường có thể xem bất kỳ.
     */
    @Transactional(readOnly = true)
    public Page<ScheduledMedicationTaskResponseDto> getStudentTaskHistory(
            Long studentId,
            ScheduledMedicationTaskStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} (Role: {}) is requesting medication history for student ID: {}, status filter: {}, date range: {} to {}",
                currentUser.getEmail(), currentUser.getRole(), studentId, status, startDate, endDate);

        // Kiểm tra học sinh có tồn tại không
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        // Kiểm tra quyền truy cập
        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.Parent) {
            // Phụ huynh chỉ được xem lịch sử của con mình
            authorizationService.authorizeParentAction(currentUser, student, "xem lịch sử uống thuốc của con");
        } else if (!(currentUserRole == UserRole.MedicalStaff ||
                currentUserRole == UserRole.StaffManager ||
                currentUserRole == UserRole.SchoolAdmin)) {
            // Các vai trò khác không được phép xem
            log.warn("User {} (Role: {}) attempted to view medication history of student (ID: {}). Access denied.",
                    currentUser.getEmail(), currentUserRole, studentId);
            throw new AccessDeniedException("Bạn không có quyền xem lịch sử uống thuốc của học sinh.");
        }

        // Sử dụng Specification để tạo query động
        Specification<ScheduledMedicationTask> spec = Specification.allOf(
                scheduledMedicationTaskSpecification.forStudent(studentId)
                .and(scheduledMedicationTaskSpecification.isHandled()).and(scheduledMedicationTaskSpecification.hasStatus(status)).and(scheduledMedicationTaskSpecification.administeredOnOrAfter(startDate)).and(scheduledMedicationTaskSpecification.administeredOnOrBefore(endDate)));

        // Thực hiện truy vấn với specification
        Page<ScheduledMedicationTask> historyPage = taskRepository.findAll(spec, pageable);

        log.info("Retrieved {} medication task history records for student ID: {} with applied filters",
                historyPage.getTotalElements(), studentId);

        return historyPage.map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ScheduledMedicationTaskResponseDto> getAllHandledTasksHistory(
            Long studentId,
            Long staffId,
            ScheduledMedicationTaskStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        User currentUser = authorizationService.getCurrentUserAndValidate();
        UserRole currentUserRole = currentUser.getRole();

        // Kiểm tra quyền - dư thừa vì @PreAuthorize ở controller đã kiểm tra, nhưng giữ lại để chắc chắn
        if (!(currentUserRole == UserRole.StaffManager || currentUserRole == UserRole.SchoolAdmin)) {
            log.warn("User {} (Role: {}) attempted to access all handled tasks history. Access denied.",
                    currentUser.getEmail(), currentUserRole);
            throw new AccessDeniedException("Chỉ Quản lý NVYT hoặc Admin trường mới có quyền xem lịch sử toàn bộ nhiệm vụ.");
        }

        // Tạo thông báo log chi tiết về các bộ lọc đang được áp dụng
        StringBuilder filterInfo = new StringBuilder("Filters applied: ");
        if (studentId != null) filterInfo.append("studentId=").append(studentId).append(", ");
        if (staffId != null) filterInfo.append("staffId=").append(staffId).append(", ");
        if (status != null) filterInfo.append("status=").append(status).append(", ");
        if (startDate != null) filterInfo.append("startDate=").append(startDate).append(", ");
        if (endDate != null) filterInfo.append("endDate=").append(endDate).append(", ");

        log.info("User {} (Role: {}) is requesting all handled tasks history. {}",
                currentUser.getEmail(), currentUserRole, filterInfo.toString());

        // Xử lý thời gian end date (nếu chỉ có date mà không có time)
        LocalDateTime effectiveEndDate = endDate;
        if (endDate != null && endDate.toLocalTime().equals(LocalTime.MIN)) {
            effectiveEndDate = endDate.with(LocalTime.MAX);
        }

        // Sử dụng Specification để tạo query động
        Specification<ScheduledMedicationTask> spec = Specification.allOf(
                scheduledMedicationTaskSpecification.isHandled().and(scheduledMedicationTaskSpecification.forStudent(studentId)).and(scheduledMedicationTaskSpecification.administeredByStaff(staffId)).and(scheduledMedicationTaskSpecification.hasStatus(status)).and(scheduledMedicationTaskSpecification.administeredOnOrAfter(startDate)).and(scheduledMedicationTaskSpecification.administeredOnOrBefore(effectiveEndDate)));
        // Thực hiện truy vấn với specification
        Page<ScheduledMedicationTask> tasksPage = taskRepository.findAll(spec, pageable);

        log.info("Retrieved {} handled task history records with applied filters",
                tasksPage.getTotalElements());

        return tasksPage.map(taskMapper::toDto);
    }
}
