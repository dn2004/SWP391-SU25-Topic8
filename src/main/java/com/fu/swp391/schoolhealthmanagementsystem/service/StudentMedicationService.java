package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.studentmedication.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.LinkStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.ScheduledMedicationTaskStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentMedicationTransactionType;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.MedicationTimeSlotMapper; // Mapper mới
import com.fu.swp391.schoolhealthmanagementsystem.mapper.StudentMedicationMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentMedicationService {

    private final StudentMedicationRepository studentMedicationRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final StudentMedicationTransactionRepository transactionRepository;
    private final ScheduledMedicationTaskRepository scheduledTaskRepository; // Thêm
    private final StudentMedicationMapper studentMedicationMapper;
    private final MedicationTimeSlotMapper medicationTimeSlotMapper; // Mapper mới
    private final AuthorizationService authorizationService;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final ScheduledTaskGenerationService scheduledTaskGenerationService; // Inject để trigger tạo task

    /**
     * NVYT tạo mới StudentMedication khi nhận thuốc trực tiếp từ phụ huynh.
     * Thuốc được coi là đã duyệt và sẵn sàng sử dụng (trạng thái AVAILABLE).
     * Một transaction INITIAL_STOCK được tạo.
     * Các ScheduledMedicationTask ban đầu sẽ được tạo nếu có đủ thông tin lịch trình.
     */
    @Transactional
    public StudentMedicationResponseDto createStudentMedicationByStaff(CreateStudentMedicationByStaffRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();

        log.info("Staff {} is attempting to create a new student medication entry.", currentStaff.getEmail());

        Student student = studentRepository.findById(requestDto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + requestDto.studentId()));

        User parent = userRepository.findByEmail(requestDto.submittedByParentEmail())
                .filter(user -> user.getRole() == UserRole.Parent)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phụ huynh với Email: " + requestDto.submittedByParentEmail() + " hoặc người dùng không phải là phụ huynh."));

        if (!parentStudentLinkRepository.existsByParentAndStudentAndStatus(parent, student, LinkStatus.ACTIVE)) {
            throw new AccessDeniedException("Phụ huynh (Email: " + parent.getEmail() + ") không có liên kết hợp lệ với học sinh (ID: " + student.getStudentId() + ").");
        }
        log.info("Parent {} is confirmed to be linked with student {}.", parent.getEmail(), student.getFullName());

        StudentMedication studentMedication = studentMedicationMapper.createDtoToEntity(requestDto, student, parent, currentStaff);

        // Explicitly set remainingDoses and status to ensure correctness
        studentMedication.setRemainingDoses(requestDto.totalDosesProvided());
        studentMedication.setStatus(MedicationStatus.AVAILABLE);
        log.info("Explicitly set remainingDoses to {} and status to AVAILABLE before saving.", requestDto.totalDosesProvided());

        StudentMedication savedMedication = studentMedicationRepository.save(studentMedication);

        if (requestDto.totalDosesProvided() > 0) {
            createInitialStockTransaction(savedMedication, requestDto.totalDosesProvided(), currentStaff, "NVYT nhập thuốc từ phụ huynh");
        }

        // Log values before task generation check
        log.info("Before task generation check - Status: {}, RemainingDoses: {}, ScheduleStartDate: {}, Has TimeSlots: {}",
                savedMedication.getStatus(),
                savedMedication.getRemainingDoses(),
                savedMedication.getScheduleStartDate(),
                savedMedication.getMedicationTimeSlots() != null && !savedMedication.getMedicationTimeSlots().isEmpty());

        // Kích hoạt tạo task ngay nếu có đủ thông tin lịch trình
        if (savedMedication.getStatus() == MedicationStatus.AVAILABLE &&
                savedMedication.getRemainingDoses() != null && savedMedication.getRemainingDoses() > 0 &&
                savedMedication.getScheduleStartDate() != null &&
                savedMedication.getMedicationTimeSlots() != null && !savedMedication.getMedicationTimeSlots().isEmpty()) {
            log.info("StudentMedication ID {} is ready for scheduling. Triggering initial task generation.", savedMedication.getStudentMedicationId());
            try {
                scheduledTaskGenerationService.generateScheduledTasks(savedMedication.getStudentMedicationId());
            } catch (Exception e) {
                log.error("Error generating initial scheduled tasks for StudentMedication ID {}: {}", savedMedication.getStudentMedicationId(), e.getMessage(), e);
                // Không ném lại lỗi để StudentMedication vẫn được tạo
            }
        } else {
            log.warn("Not generating tasks because one of the conditions is not met: Status: {}, RemainingDoses: {}, ScheduleStartDate: {}, Has TimeSlots: {}",
                    savedMedication.getStatus(),
                    savedMedication.getRemainingDoses(),
                    savedMedication.getScheduleStartDate(),
                    savedMedication.getMedicationTimeSlots() != null && !savedMedication.getMedicationTimeSlots().isEmpty());
        }

        log.info("StudentMedication ID {} created by staff {}. Medication: '{}', Total Doses: {}, RemainingDoses: {}. Status: {}.",
                savedMedication.getStudentMedicationId(), currentStaff.getEmail(), savedMedication.getMedicationName(),
                savedMedication.getTotalDosesProvided(), savedMedication.getRemainingDoses(), savedMedication.getStatus());

        return studentMedicationMapper.entityToResponseDto(savedMedication);
    }

    /**
     * Lấy thông tin chi tiết một bản ghi StudentMedication bằng ID.
     */
    @Transactional(readOnly = true)
    public StudentMedicationResponseDto getStudentMedicationById(Long studentMedicationId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} (Role: {}) is requesting StudentMedication with ID: {}",
                currentUser.getEmail(), currentUser.getRole(), studentMedicationId);

        // Sử dụng query fetch join để lấy time slots nếu cần
        StudentMedication studentMedication = studentMedicationRepository.findWithMedicationTimeSlotsByStudentMedicationId(studentMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thuốc của học sinh với ID: " + studentMedicationId));

        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.Parent) {
            Student studentOfMedication = studentMedication.getStudent();
            if (studentOfMedication == null) {
                throw new IllegalStateException("Dữ liệu thuốc không hợp lệ, không tìm thấy thông tin học sinh liên quan.");
            }
            authorizationService.authorizeParentAction(currentUser, studentOfMedication, "xem thông tin thuốc của con");
        } else if (!(currentUserRole == UserRole.MedicalStaff ||
                currentUserRole == UserRole.StaffManager ||
                currentUserRole == UserRole.SchoolAdmin)) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin thuốc này.");
        }
        return studentMedicationMapper.entityToResponseDto(studentMedication);
    }

    /**
     * NVYT cập nhật thông tin lịch trình của một StudentMedication.
     * Sẽ hủy các task SCHEDULED trong tương lai và kích hoạt tạo lại task mới.
     */
    @Transactional
    public StudentMedicationResponseDto updateMedicationSchedule(Long studentMedicationId, UpdateMedicationScheduleRequestDto scheduleDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        log.info("Staff {} is attempting to update schedule for StudentMedication ID: {}",
                currentStaff.getEmail(), studentMedicationId);

        StudentMedication medication = studentMedicationRepository.findWithMedicationTimeSlotsByStudentMedicationId(studentMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thuốc với ID: " + studentMedicationId));

        //chỉ cho phép cập nhật lịch trình nếu thuốc đang có trạng thái AVAILABLE
        if (medication.getStatus() != MedicationStatus.AVAILABLE) {
            log.warn("Cannot update schedule for StudentMedication ID {} with status {}.", studentMedicationId, medication.getStatus());
            throw new IllegalStateException("Chỉ có thể cập nhật lịch trình cho thuốc đang có trạng thái 'Sẵn có'. Trạng thái hiện tại: " + medication.getStatus().getDisplayName());
        }

        // 1. Hủy các ScheduledMedicationTask trong tương lai (status SCHEDULED) dựa trên lịch cũ
        cancelFutureScheduledTasks(medication, currentStaff, "Lịch trình thuốc được cập nhật bởi NVYT");

        // 2. Cập nhật thông tin lịch trình trên StudentMedication
        // Xử lý medicationTimeSlots: xóa cũ, thêm mới từ DTO
        medication.clearMedicationTimeSlots(); // Gọi helper method đã tạo trong StudentMedication
        if (scheduleDto.scheduleTimes() != null && !scheduleDto.scheduleTimes().isEmpty()) {
            for (MedicationTimeSlotDto slotDto : scheduleDto.scheduleTimes()) {
                MedicationTimeSlot newSlot = medicationTimeSlotMapper.toEntity(slotDto);
                medication.addMedicationTimeSlot(newSlot); // Helper method này sẽ set newSlot.setStudentMedication(medication)
            }
        }
        medication.setScheduleStartDate(scheduleDto.scheduleStartDate());
        medication.setUpdatedByUser(currentStaff);
        medication.setNextScheduledTaskGenerationDate(null); // Reset để job nền hoặc trigger ngay có thể chạy lại

        StudentMedication updatedMedication = studentMedicationRepository.save(medication);
        log.info("Schedule for StudentMedication ID {} updated by {}.",
                updatedMedication.getStudentMedicationId(), currentStaff.getEmail());

        // 3. Kích hoạt tạo lại task mới nếu lịch vẫn hợp lệ để chạy
        if (updatedMedication.getStatus() == MedicationStatus.AVAILABLE){
            if (updatedMedication.getRemainingDoses() != null && updatedMedication.getRemainingDoses() > 0 &&
                    updatedMedication.getScheduleStartDate() != null &&
                    updatedMedication.getMedicationTimeSlots() != null && !updatedMedication.getMedicationTimeSlots().isEmpty()) {
                log.info("Schedule updated for StudentMedication ID {}. Triggering task regeneration.", updatedMedication.getStudentMedicationId());
                try {
                    scheduledTaskGenerationService.generateScheduledTasks(updatedMedication.getStudentMedicationId());
                } catch (Exception e) {
                    log.error("Error regenerating scheduled tasks after schedule update for StudentMedication ID {}: {}", updatedMedication.getStudentMedicationId(), e.getMessage(), e);
                }
            } else {
                log.info("StudentMedication ID {} schedule updated, but conditions not met for immediate task regeneration (e.g. no remaining doses or missing schedule info).", updatedMedication.getStudentMedicationId());
            }
        }
        return studentMedicationMapper.entityToResponseDto(updatedMedication);
    }

    @Transactional
    public StudentMedicationResponseDto updateStudentMedicationInfo(Long studentMedicationId, UpdateStudentMedicationInfoRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        // Quyền của currentStaff (MedicalStaff, StaffManager) sẽ được kiểm tra ở Controller bằng @PreAuthorize

        log.info("Staff {} is attempting to update general info for StudentMedication ID: {}",
                currentStaff.getEmail(), studentMedicationId);

        // Lấy entity, không cần fetch join timeSlots vì không sửa chúng ở đây
        StudentMedication medication = studentMedicationRepository.findById(studentMedicationId)
                .orElseThrow(() -> {
                    log.warn("Update info failed: StudentMedication not found with ID: {}", studentMedicationId);
                    return new ResourceNotFoundException("Không tìm thấy thông tin thuốc của học sinh với ID: " + studentMedicationId);
                });

        // Kiểm tra các điều kiện có thể ngăn cản việc cập nhật (tùy nghiệp vụ)
        // Ví dụ: không cho cập nhật thuốc đã hết hẳn, đã trả, hoặc đã bị từ chối ban đầu
        if (medication.getStatus() == MedicationStatus.OUT_OF_DOSES ||
                medication.getStatus() == MedicationStatus.RETURNED_TO_PARENT ||
                medication.getStatus() == MedicationStatus.EXPIRED ||
                medication.getStatus() == MedicationStatus.LOST) {
            log.warn("Update info failed: StudentMedication ID {} has status {} which does not allow general info updates.",
                    studentMedicationId, medication.getStatus());
            throw new IllegalStateException("Không thể cập nhật thông tin cho thuốc có trạng thái: " +
                    (medication.getStatus() != null ? medication.getStatus().getDisplayName() : "Không xác định"));
        }

        // Sử dụng mapper để cập nhật các trường từ DTO vào entity
        // @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE) trong mapper
        // sẽ đảm bảo chỉ các trường non-null trong DTO được cập nhật.
        studentMedicationMapper.updateMedicationInfoFromDto(requestDto, medication);
        medication.setUpdatedByUser(currentStaff);
        // updatedAt sẽ được @UpdateTimestamp tự động xử lý khi save

        StudentMedication updatedMedication = studentMedicationRepository.save(medication);

        log.info("StudentMedication ID {} general info updated successfully by staff {}",
                updatedMedication.getStudentMedicationId(), currentStaff.getEmail());

        // Khi trả về, mapper sẽ tự động lấy thông tin timeSlots hiện có (nếu có)
        return studentMedicationMapper.entityToResponseDto(updatedMedication);
    }

    @Transactional
    public StudentMedicationResponseDto reportLostMedication(Long studentMedicationId, ReportLostMedicationRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        log.info("Staff {} is reporting medication ID: {} as LOST. Notes: {}",
                currentStaff.getEmail(), studentMedicationId, requestDto.staffNotes());

        StudentMedication medication = studentMedicationRepository.findById(studentMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thuốc với ID: " + studentMedicationId));

        if (medication.getStatus() == MedicationStatus.LOST ||
                medication.getStatus() == MedicationStatus.RETURNED_TO_PARENT ||
                medication.getStatus() == MedicationStatus.OUT_OF_DOSES) {
            throw new IllegalStateException("Không thể báo thất lạc cho thuốc đang có trạng thái: " + medication.getStatus().getDisplayName());
        }

        int dosesLost = medication.getRemainingDoses() != null ? medication.getRemainingDoses() : 0;

        medication.setStatus(MedicationStatus.LOST);
        medication.setRemainingDoses(0); // Khi thất lạc, coi như mất hết số liều còn lại
        medication.setUpdatedByUser(currentStaff);
        // Ghi chú về việc thất lạc có thể thêm vào medication.setNotes() hoặc chỉ trong transaction
        if (requestDto.staffNotes() != null && !requestDto.staffNotes().isBlank()) {
            String existingNotes = medication.getNotes() == null ? "" : medication.getNotes() + "\n";
            medication.setNotes(existingNotes + "Báo thất lạc bởi " + currentStaff.getFullName() + " lúc " + LocalDate.now() + ": " + requestDto.staffNotes());
        }


        StudentMedication updatedMedication = studentMedicationRepository.save(medication);

        // Tạo transaction ghi nhận thất lạc (nếu có liều bị mất)
        if (dosesLost > 0) {
            createMedicationAdjustmentTransaction(
                    updatedMedication,
                    StudentMedicationTransactionType.LOST,
                    dosesLost, // Số lượng bị mất (là số dương)
                    currentStaff,
                    "Thuốc bị thất lạc. " + (requestDto.staffNotes() != null ? requestDto.staffNotes() : "")
            );
        }

        // Hủy các task tương lai
        cancelFutureScheduledTasks(updatedMedication, currentStaff, "Thuốc đã được báo cáo thất lạc");

        log.info("StudentMedication ID {} reported as LOST by {}. Remaining doses set to 0. Future tasks cancelled.",
                updatedMedication.getStudentMedicationId(), currentStaff.getEmail());
        return studentMedicationMapper.entityToResponseDto(updatedMedication);
    }

    /**
     * NVYT xác nhận đã trả thuốc cho phụ huynh.
     * Cập nhật trạng thái, số liều còn lại về 0, tạo transaction, và hủy các task tương lai.
     */
    @Transactional
    public StudentMedicationResponseDto returnMedicationToParent(Long studentMedicationId, ReturnMedicationToParentRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        log.info("Staff {} is returning medication ID: {} to parent. Notes: {}",
                currentStaff.getEmail(), studentMedicationId, requestDto.staffNotes());

        StudentMedication medication = studentMedicationRepository.findById(studentMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thuốc với ID: " + studentMedicationId));

        if (medication.getStatus() == MedicationStatus.RETURNED_TO_PARENT ||
                medication.getStatus() == MedicationStatus.LOST ||
                medication.getStatus() == MedicationStatus.OUT_OF_DOSES
        ) {
            throw new IllegalStateException("Không thể trả lại thuốc đang có trạng thái: " + medication.getStatus().getDisplayName());
        }

        int dosesReturned = medication.getRemainingDoses() != null ? medication.getRemainingDoses() : 0;

        medication.setStatus(MedicationStatus.RETURNED_TO_PARENT);
        medication.setRemainingDoses(0);
        medication.setUpdatedByUser(currentStaff);
        if (requestDto.staffNotes() != null && !requestDto.staffNotes().isBlank()) {
            String existingNotes = medication.getNotes() == null ? "" : medication.getNotes() + "\n";
            medication.setNotes(existingNotes + "Đã trả lại phụ huynh bởi " + currentStaff.getFullName() + " lúc " + LocalDate.now() + ": " + requestDto.staffNotes());
        }

        StudentMedication updatedMedication = studentMedicationRepository.save(medication);

        // Tạo transaction ghi nhận trả thuốc (nếu có liều được trả)
        if (dosesReturned > 0) {
            createMedicationAdjustmentTransaction(
                    updatedMedication,
                    StudentMedicationTransactionType.RETURNED_TO_PARENT,
                    dosesReturned, // Số lượng trả lại (là số dương)
                    currentStaff,
                    "Đã trả thuốc cho phụ huynh. " + (requestDto.staffNotes() != null ? requestDto.staffNotes() : "")
            );
        }

        // Hủy các task tương lai
        cancelFutureScheduledTasks(updatedMedication, currentStaff, "Thuốc đã được trả lại phụ huynh");

        log.info("StudentMedication ID {} marked as RETURNED_TO_PARENT by {}. Remaining doses set to 0. Future tasks cancelled.",
                updatedMedication.getStudentMedicationId(), currentStaff.getEmail());
        return studentMedicationMapper.entityToResponseDto(updatedMedication);
    }


    /**
     * NVYT hủy một StudentMedication (thu hồi) với điều kiện chưa được lên lịch.
     * Chỉ người tạo ra StudentMedication mới có thể hủy.
     * Một transaction CANCELLATION_REVERSAL được tạo để ghi nhận sự kiện hủy và lý do.
     */
    @Transactional
    public StudentMedicationResponseDto cancelStudentMedication(Long studentMedicationId, CancelStudentMedicationRequestDto requestDto) {
        User currentStaff = authorizationService.getCurrentUserAndValidate();
        log.info("Staff {} is attempting to cancel medication ID: {}. Reason: {}",
                currentStaff.getEmail(), studentMedicationId, requestDto.cancellationReason());

        StudentMedication medication = studentMedicationRepository.findById(studentMedicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin thuốc với ID: " + studentMedicationId));

        // Kiểm tra xem người dùng hiện tại có phải là người đã tạo thuốc không
        if (medication.getCreatedByUser() == null || !medication.getCreatedByUser().getUserId().equals(currentStaff.getUserId())) {
            log.warn("Cancel medication failed: Staff {} is not the creator of medication {}",
                    currentStaff.getEmail(), studentMedicationId);
            throw new AccessDeniedException("Chỉ nhân viên y tế đã tạo thuốc này mới có thể hủy.");
        }

        // Kiểm tra xem thuốc đã được lên lịch chưa
        boolean hasScheduledTasks = scheduledTaskRepository.existsAdministeredTasksByStudentMedicationId(studentMedicationId);
        if (hasScheduledTasks) {
            log.warn("Cancel medication failed: Medication ID {} already has scheduled tasks",
                    studentMedicationId);
            throw new IllegalStateException("Không thể hủy thuốc vì nhân viên y tế đã cho học sinh uống thuốc");
        }

        // Kiểm tra trạng thái thuốc
        if (medication.getStatus() != MedicationStatus.AVAILABLE) {
            throw new IllegalStateException("Chỉ có thể hủy thuốc đang ở trạng thái 'Sẵn có'. Trạng thái hiện tại: " +
                    medication.getStatus().getDisplayName());
        }

        // Lưu lại số liều trước khi cập nhật để tạo transaction
        int dosingCancelled = medication.getRemainingDoses() != null ? medication.getRemainingDoses() : 0;

        // Cập nhật thông tin thuốc
        medication.setRemainingDoses(0);
        medication.setStatus(MedicationStatus.CANCEL); // Sử dụng trạng thái RETURNED_TO_PARENT để đánh dấu là đã trả/hủy
        medication.setUpdatedByUser(currentStaff);

        // Thêm ghi chú về việc hủy
        String cancellationNote = "Thuốc đã bị hủy bởi " + currentStaff.getFullName() + " lúc " + LocalDate.now() +
                ". Lý do: " + requestDto.cancellationReason();
        medication.setNotes(medication.getNotes() != null ? medication.getNotes() + "\n" + cancellationNote : cancellationNote);

        StudentMedication cancelledMedication = studentMedicationRepository.save(medication);

        // Tạo transaction ghi nhận việc hủy thuốc
        if (dosingCancelled > 0) {
            createMedicationAdjustmentTransaction(
                    cancelledMedication,
                    StudentMedicationTransactionType.CANCELLATION_REVERSAL,
                    dosingCancelled,
                    currentStaff,
                    "Hủy thuốc. Lý do: " + requestDto.cancellationReason()
            );
        }

        log.info("StudentMedication ID {} successfully cancelled by {}. Cancellation reason: {}",
                cancelledMedication.getStudentMedicationId(), currentStaff.getEmail(), requestDto.cancellationReason());

        return studentMedicationMapper.entityToResponseDto(cancelledMedication);
    }

    /**
     * Lấy danh sách thuốc (có phân trang) của một học sinh cụ thể.
     * Phụ huynh chỉ xem được thuốc của con mình.
     * NVYT, Quản lý NVYT, Admin trường có thể xem bất kỳ.
     */
    @Transactional(readOnly = true)
    public Page<StudentMedicationResponseDto> getMedicationsByStudentId(Long studentId, Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} (Role: {}) is requesting medications for student ID: {}",
                currentUser.getEmail(), currentUser.getRole(), studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        // Kiểm tra quyền: Phụ huynh chỉ xem được thuốc của con mình
        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem thông tin thuốc của con");
        } else if (!(currentUserRole == UserRole.MedicalStaff ||
                currentUserRole == UserRole.StaffManager ||
                currentUserRole == UserRole.SchoolAdmin)) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin thuốc của học sinh này.");
        }

        Page<StudentMedication> medicationsPage = studentMedicationRepository.findByStudent(student, pageable);

        log.info("Retrieved {} student medications for student ID: {}",
                medicationsPage.getTotalElements(), studentId);

        return medicationsPage.map(studentMedicationMapper::entityToResponseDto);
    }

    /**
     * Lấy danh sách thuốc (có phân trang) của một học sinh cụ thể với lọc theo khoảng thời gian.
     * Phụ huynh chỉ xem được thuốc của con mình.
     * NVYT, Quản lý NVYT, Admin trường có thể xem bất kỳ.
     */
    @Transactional(readOnly = true)
    public Page<StudentMedicationResponseDto> getMedicationsByStudentId(
            Long studentId, 
            Optional<LocalDate> startDateOpt,
            Optional<LocalDate> endDateOpt,
            Pageable pageable) {
            
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} (Role: {}) is requesting medications for student ID: {}, date range: {} to {}",
                currentUser.getEmail(), currentUser.getRole(), studentId, 
                startDateOpt.orElse(null), endDateOpt.orElse(null));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        // Kiểm tra quyền: Phụ huynh chỉ xem được thuốc của con mình
        UserRole currentUserRole = currentUser.getRole();
        if (currentUserRole == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem thông tin thuốc của con");
        } else if (!(currentUserRole == UserRole.MedicalStaff ||
                currentUserRole == UserRole.StaffManager ||
                currentUserRole == UserRole.SchoolAdmin)) {
            throw new AccessDeniedException("Bạn không có quyền xem thông tin thuốc của học sinh này.");
        }


        LocalDate startDate = startDateOpt.orElse(null);
        LocalDate endDate = endDateOpt.orElse(null);

       Page<StudentMedication> medicationsPage;
        
        if (startDate != null || endDate != null) {
            medicationsPage = studentMedicationRepository.findByStudentAndDateRange(student, startDate, endDate, pageable);
            log.info("Retrieved {} student medications for student ID: {} with date filter", 
                    medicationsPage.getTotalElements(), studentId);
        } else {
            medicationsPage = studentMedicationRepository.findByStudent(student, pageable);
            log.info("Retrieved {} student medications for student ID: {}", 
                    medicationsPage.getTotalElements(), studentId);
        }
        
        return medicationsPage.map(studentMedicationMapper::entityToResponseDto);
    }

    /**
     * Lấy tất cả các bản ghi thuốc trong hệ thống, có phân trang và lọc theo trạng thái và khoảng thời gian.
     * Chỉ cho phép nhân viên y tế, quản lý nhân viên, hoặc admin trường truy cập.
     */
    @Transactional(readOnly = true)
    public Page<StudentMedicationResponseDto> getAllStudentMedications(
            Optional<MedicationStatus> statusOpt,
            Optional<LocalDate> startDateOpt,
            Optional<LocalDate> endDateOpt,
            Pageable pageable) {

        User currentUser = authorizationService.getCurrentUserAndValidate();

        MedicationStatus status = statusOpt.orElse(null);
        LocalDate startDate = startDateOpt.orElse(null);
        LocalDate endDate = endDateOpt.orElse(null);

        log.info("User {} (Role: {}) is requesting all student medications, status filter: {}, date range: {} to {}",
                currentUser.getEmail(), currentUser.getRole(), status, startDate, endDate);

        // Quyền truy cập đã được kiểm tra ở Controller bằng @PreAuthorize
        Page<StudentMedication> medicationsPage;

        // Xác định phương thức repository cần gọi dựa trên tham số
        if (status != null && (startDate != null || endDate != null)) {
            // Lọc theo cả status và date range
            medicationsPage = studentMedicationRepository.findByStatusAndDateRange(status,  startDate, endDate, pageable);
            log.info("Retrieved {} student medications with status: {} and date range",
                    medicationsPage.getTotalElements(), status);
        } else if (status != null) {
            // Chỉ lọc theo status
            medicationsPage = studentMedicationRepository.findByStatus(status, pageable);
            log.info("Retrieved {} student medications with status: {}",
                    medicationsPage.getTotalElements(), status);
        } else if (startDate != null || endDate != null) {
            // Chỉ lọc theo date range
            medicationsPage = studentMedicationRepository.findByDateRange(startDate, endDate, pageable);
            log.info("Retrieved {} student medications with date range",
                    medicationsPage.getTotalElements());
        } else {
            // Không lọc gì cả
            medicationsPage = studentMedicationRepository.findAll(pageable);
            log.info("Retrieved {} student medications (all statuses)",
                    medicationsPage.getTotalElements());
        }

        return medicationsPage.map(studentMedicationMapper::entityToResponseDto);
    }

    // Helper method để tạo transaction điều chỉnh (LOST, RETURNED_TO_PARENT, EXPIRED_REMOVAL, etc.)
    private void createMedicationAdjustmentTransaction(StudentMedication medication,
                                                       StudentMedicationTransactionType type,
                                                       int dosesChangedPositive, // Luôn là số dương thể hiện lượng bị ảnh hưởng
                                                       User performedBy,
                                                       String notes) {
        if (dosesChangedPositive <= 0) return; // Không tạo transaction nếu không có liều nào thay đổi

        StudentMedicationTransaction transaction = StudentMedicationTransaction.builder()
                .studentMedication(medication)
                .transactionType(type)
                .dosesChanged(dosesChangedPositive) // Lưu số lượng bị ảnh hưởng
                .performedByUser(performedBy)
                .notes(notes)
                .build();
        transactionRepository.save(transaction);
        log.info("{} transaction created for StudentMedication ID {} with {} doses affected.",
                type, medication.getStudentMedicationId(), dosesChangedPositive);
    }


    private void createInitialStockTransaction(StudentMedication medication, Integer totalDoses, User performedBy, String notes) {
        if (totalDoses == null || totalDoses <= 0) {
            log.warn("Skipping initial stock transaction for StudentMedication ID {} as totalDoses is not positive: {}", medication.getStudentMedicationId(), totalDoses);
            return;
        }
        StudentMedicationTransaction transaction = StudentMedicationTransaction.builder()
                .studentMedication(medication)
                .transactionType(StudentMedicationTransactionType.INITIAL_STOCK)
                .dosesChanged(totalDoses)
                .performedByUser(performedBy)
                .notes(notes)
                .build();
        transactionRepository.save(transaction);
        log.info("INITIAL_STOCK transaction created for StudentMedication ID {} with {} doses.", medication.getStudentMedicationId(), totalDoses);
    }

    private void cancelFutureScheduledTasks(StudentMedication medication, User staff, String reason) {
        List<ScheduledMedicationTask> futureTasks = scheduledTaskRepository
                .findByStudentMedicationAndScheduledDateGreaterThanEqualAndStatus(
                        medication,
                        LocalDate.now(),
                        ScheduledMedicationTaskStatus.SCHEDULED
                );

        if (!futureTasks.isEmpty()) {
            for (ScheduledMedicationTask task : futureTasks) {
                task.setStatus(ScheduledMedicationTaskStatus.UPDATED_TO_ANOTHER_RECORD); // Hoặc một status mới
                task.setStaffNotes((task.getStaffNotes() == null ? "" : task.getStaffNotes() + "\n") +
                        "Hủy tự động do lịch gốc thay đổi. Lý do: " + reason + ". Bởi: " + staff.getFullName() + " lúc " + LocalDate.now());
                task.setAdministeredByStaff(staff);
            }
            scheduledTaskRepository.saveAll(futureTasks);
            log.info("Cancelled {} future scheduled tasks for StudentMedication ID {} due to schedule update.",
                    futureTasks.size(), medication.getStudentMedicationId());
        }
    }


}
