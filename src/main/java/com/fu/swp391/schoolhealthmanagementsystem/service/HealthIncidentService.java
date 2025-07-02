package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.UpdateHealthIncidentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.HealthIncidentType;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.MedicalSupplyStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.SupplyTransactionType;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.CreateHealthIncidentRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.HealthIncidentResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.incident.HealthIncidentSupplyUsageDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.*;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.HealthIncidentMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.HealthIncidentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.MedicalSupplyRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.SupplyTransactionRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.HealthIncidentSpecification;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthIncidentService {

    private final HealthIncidentRepository healthIncidentRepository;
    private final StudentRepository studentRepository;
    private final MedicalSupplyRepository medicalSupplyRepository;
    private final MedicalSupplyService medicalSupplyService;
    private final HealthIncidentMapper healthIncidentMapper;
    private final AuthorizationService authorizationService;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final HealthIncidentSpecification healthIncidentSpecification;
    private final NotificationService notificationService; // Inject NotificationService


    // --- CREATE ---
    @Transactional
    public HealthIncidentResponseDto createHealthIncident(CreateHealthIncidentRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} is creating a health incident for student ID: {}", currentUser.getEmail(), requestDto.studentId());

        Student student = studentRepository.findById(requestDto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + requestDto.studentId()));

        HealthIncident incident = healthIncidentMapper.toEntity(requestDto);
        incident.setStudent(student);
        incident.setRecordedByUser(currentUser); // Gán người tạo
        // incident.setDeleted(false); // Mapper đã set hoặc @Builder.Default

        // Pre-check supply availability
        if (requestDto.supplyUsages() != null && !requestDto.supplyUsages().isEmpty()) {
            for (HealthIncidentSupplyUsageDto usageDto : requestDto.supplyUsages()) {
                MedicalSupply supply = medicalSupplyRepository.findById(usageDto.supplyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Medical supply not found with ID: " + usageDto.supplyId()));

                if (supply.getStatus() == MedicalSupplyStatus.DISPOSE || supply.getStatus() == MedicalSupplyStatus.EXPIRED) {
                    throw new IllegalStateException("Medical supply '" + supply.getName() + "' (ID: " + supply.getSupplyId() +
                            ") has status " + supply.getStatus() + " and cannot be used.");
                }

                if (supply.getCurrentStock() < usageDto.quantityUsed()) {
                    throw new IllegalStateException("Insufficient stock for medical supply '" + supply.getName() + "'. Requested: " +
                            usageDto.quantityUsed() + ", Available: " + supply.getCurrentStock());
                }
            }
        }

        HealthIncident savedIncident = healthIncidentRepository.save(incident);

        // Ensure all SupplyTransaction entities are saved before associating them with the HealthIncident
        if (requestDto.supplyUsages() != null && !requestDto.supplyUsages().isEmpty()) {
            for (HealthIncidentSupplyUsageDto usageDto : requestDto.supplyUsages()) {
                MedicalSupply supply = medicalSupplyRepository.findById(usageDto.supplyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Medical supply not found with ID: " + usageDto.supplyId()));

                if (supply.getStatus() == MedicalSupplyStatus.DISPOSE || supply.getStatus() == MedicalSupplyStatus.EXPIRED) {
                    throw new IllegalStateException("Medical supply '" + supply.getName() + "' (ID: " + supply.getSupplyId() +
                            ") has status " + supply.getStatus() + " and cannot be used.");
                }

                if (supply.getCurrentStock() < usageDto.quantityUsed()) {
                    throw new IllegalStateException("Insufficient stock for medical supply '" + supply.getName() + "'. Requested: " +
                            usageDto.quantityUsed() + ", Available: " + supply.getCurrentStock());
                }

                // Create and save the SupplyTransaction before associating it
                SupplyTransaction transaction = new SupplyTransaction();
                transaction.setMedicalSupply(supply);
                transaction.setQuantity(usageDto.quantityUsed());
                transaction.setSupplyTransactionType(SupplyTransactionType.USED_FOR_INCIDENT);
                transaction.setHealthIncident(savedIncident);
                transaction.setPerformedByUser(currentUser);
                supplyTransactionRepository.save(transaction);

                // Update the supply stock
                int oldStock = supply.getCurrentStock();
                supply.setCurrentStock(oldStock - usageDto.quantityUsed());

                // Cập nhật trạng thái nếu hết hàng
                if (supply.getCurrentStock() == 0) {
                    supply.setStatus(MedicalSupplyStatus.OUT_OF_STOCK);
                    log.info("Medical supply ID {} is now out of stock, updating status to OUT_OF_STOCK", supply.getSupplyId());
                }

                medicalSupplyRepository.save(supply);
            }
        }

        // Fetch again to get the fully populated incident with transactions for the response
        HealthIncident finalIncidentWithTransactions = healthIncidentRepository.findById(savedIncident.getIncidentId())
                .orElseThrow(() -> new IllegalStateException("Could not retrieve the newly created incident: " + savedIncident.getIncidentId()));

        log.info("Health incident ID: {} created for student {}, recorded by {}",
                finalIncidentWithTransactions.getIncidentId(), student.getFullName(), currentUser.getFullName());

        // Send notification to parent
        sendIncidentCreationNotification(finalIncidentWithTransactions);

        return healthIncidentMapper.toDto(finalIncidentWithTransactions);
    }

    private void sendIncidentCreationNotification(HealthIncident incident) {
        try {
            String content = String.format("Một sự cố sức khỏe vừa được ghi nhận cho học sinh '%s'.",
                    incident.getStudent().getFullName());
            String link = "/health-incidents/" + incident.getIncidentId();
            // Người gửi là người ghi nhận sự cố
            String sender = incident.getRecordedByUser() != null ? incident.getRecordedByUser().getEmail() : "system";

            sendNotificationToParents(incident.getStudent(), content, link, sender, "tạo mới sự cố");
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo tạo mới sự cố sức khỏe ID {}: {}", incident.getIncidentId(), e.getMessage(), e);
        }
    }

    // --- READ ---
    @Transactional(readOnly = true)
    public HealthIncidentResponseDto getHealthIncidentById(Long incidentId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        HealthIncident incident = healthIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Health incident not found with ID: " + incidentId));

        Student studentOfIncident = incident.getStudent();
        if (studentOfIncident == null) {
            // Trường hợp này không nên xảy ra nếu DB constraint đúng
            log.error("Health incident ID {} is not associated with any student.", incidentId);
            throw new IllegalStateException("Health incident data is corrupted: no associated student.");
        }

        // Authorization check
        if (currentUser.getRole() == UserRole.Parent) {
            // Sử dụng hàm đã có trong AuthorizationService
            authorizationService.authorizeParentAction(currentUser, studentOfIncident, "xem chi tiết sự cố sức khỏe");
        } else if (!(currentUser.getRole() == UserRole.MedicalStaff ||
                currentUser.getRole() == UserRole.StaffManager ||
                currentUser.getRole() == UserRole.SchoolAdmin)) {
            log.warn("User {} with role {} attempted to access health incident {} without sufficient privileges.",
                    currentUser.getEmail(), currentUser.getRole(), incidentId);
            throw new AccessDeniedException("You do not have permission to view this health incident.");
        }
        log.info("User {} retrieved health incident ID: {}", currentUser.getEmail(), incidentId);
        return healthIncidentMapper.toDto(incident);
    }

    @Transactional(readOnly = true)
    public Page<HealthIncidentResponseDto> getAllHealthIncidentsByStudentId(Long studentId,
                                                                            Pageable pageable,
                                                                            HealthIncidentType incidentType,
                                                                            String location,
                                                                            LocalDate startDate,
                                                                            LocalDate endDate) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        if (currentUser.getRole() == UserRole.Parent) {
            // Sử dụng hàm đã có trong AuthorizationService
            authorizationService.authorizeParentAction(currentUser, student, "xem danh sách sự cố sức khỏe");
        } else if (!(currentUser.getRole() == UserRole.MedicalStaff ||
                currentUser.getRole() == UserRole.StaffManager ||
                currentUser.getRole() == UserRole.SchoolAdmin)) {
            log.warn("User {} with role {} attempted to access health incidents for student {} without sufficient privileges.",
                    currentUser.getEmail(), currentUser.getRole(), studentId);
            throw new AccessDeniedException("You do not have permission to view health incidents for this student.");
        }

        log.info("User {} retrieving health incidents for student ID: {} with filters - Type: {}, Location: {}, StartDate: {}, EndDate: {}",
                currentUser.getEmail(), studentId, incidentType, location, startDate, endDate);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999999999) : null;

        Specification<HealthIncident> spec = Specification
                .allOf(healthIncidentSpecification.forStudent(studentId))
                .and(healthIncidentSpecification.isNotDeleted())
                .and(healthIncidentSpecification.hasType(incidentType))
                .and(healthIncidentSpecification.hasLocationContaining(location))
                .and(healthIncidentSpecification.happenedOnOrAfter(startDateTime))
                .and(healthIncidentSpecification.happenedOnOrBefore(endDateTime));
        Page<HealthIncident> incidentsPage = healthIncidentRepository.findAll(spec, pageable);
        return incidentsPage.map(healthIncidentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<HealthIncidentResponseDto> getAllHealthIncidents(Pageable pageable,
                                                                 HealthIncidentType incidentType,
                                                                 LocalDate startDate,
                                                                 LocalDate endDate,
                                                                 String studentName,
                                                                 String recordedByName,
                                                                 String location,
                                                                 String description) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // Authorization for these roles will be handled by @PreAuthorize in controller
        // e.g., @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")

        log.info("User {} retrieving all health incidents with filters - Type: {}, StartDate: {}, EndDate: {}, StudentName: {}, RecordedByName: {}, Location: {}, Description: {}",
                currentUser.getEmail(), incidentType, startDate, endDate, studentName, recordedByName, location, description);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999999999) : null;

        Specification<HealthIncident> spec = Specification
                .allOf(healthIncidentSpecification.isNotDeleted())
                .and(healthIncidentSpecification.forStudentName(studentName))
                .and(healthIncidentSpecification.recordedByName(recordedByName))
                .and(healthIncidentSpecification.hasType(incidentType))
                .and(healthIncidentSpecification.happenedOnOrAfter(startDateTime))
                .and(healthIncidentSpecification.happenedOnOrBefore(endDateTime))
                .and(healthIncidentSpecification.hasLocationContaining(location))
                .and(healthIncidentSpecification.descriptionContaining(description));


        return healthIncidentRepository.findAll(spec, pageable)
                .map(healthIncidentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<HealthIncidentResponseDto> getMyHealthIncidents(Pageable pageable,
                                                              HealthIncidentType incidentType,
                                                              LocalDate startDate,
                                                              LocalDate endDate,
                                                              String studentName,
                                                              String location,
                                                              String description) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // @PreAuthorize("hasRole('MedicalStaff')") in controller will handle authorization

        log.info("User {} retrieving their own health incidents with filters - Type: {}, StartDate: {}, EndDate: {}, StudentName: {}, Location: {}, Description: {}",
                currentUser.getEmail(), incidentType, startDate, endDate, studentName, location, description);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999999999) : null;

        Specification<HealthIncident> spec = Specification
                .allOf(healthIncidentSpecification.isNotDeleted())
                .and(healthIncidentSpecification.recordedBy(currentUser.getUserId())) // Filter by current user's ID
                .and(healthIncidentSpecification.forStudentName(studentName))
                .and(healthIncidentSpecification.hasType(incidentType))
                .and(healthIncidentSpecification.happenedOnOrAfter(startDateTime))
                .and(healthIncidentSpecification.happenedOnOrBefore(endDateTime))
                .and(healthIncidentSpecification.hasLocationContaining(location))
                .and(healthIncidentSpecification.descriptionContaining(description));


        return healthIncidentRepository.findAll(spec, pageable)
                .map(healthIncidentMapper::toDto);
    }

    // --- UPDATE ---
    @Transactional
    public HealthIncidentResponseDto updateHealthIncident(Long incidentId, UpdateHealthIncidentRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        HealthIncident incident = healthIncidentRepository.findById(incidentId) // Sẽ không tìm thấy nếu is_deleted = true
                .orElseThrow(() -> new ResourceNotFoundException("Health incident not found with ID: " + incidentId));

        User recordedUser = incident.getRecordedByUser();

        // Check if update is allowed (within 1 day of creation)
        if (incident.getCreatedAt().isBefore(LocalDateTime.now().minusDays(1))) {
            log.warn("User {} attempted to update health incident ID {} which was created more than 1 day ago.",
                    currentUser.getEmail(), incidentId);
            throw new IllegalStateException("Cannot update a health incident that was created more than 1 day ago.");
        }

        // Authorization: Only the recorder or specific admin roles can update
        boolean canUpdate = false;
        if (currentUser.getRole() == UserRole.StaffManager || currentUser.getRole() == UserRole.SchoolAdmin) {
            canUpdate = true;
        } else if (currentUser.getRole() == UserRole.MedicalStaff) {
            if (currentUser.getUserId().equals(recordedUser.getUserId())) {
                canUpdate = true;
            }
        }

        if (!canUpdate) {
            log.warn("User {} (Role: {}) attempted to update health incident ID {} without permission.",
                    currentUser.getEmail(), currentUser.getRole(), incidentId);
            throw new AccessDeniedException("You do not have permission to update this health incident.");
        }

        healthIncidentMapper.updateEntityFromDto(updateDto, incident);
        incident.setUpdatedByUser(currentUser);

        HealthIncident updatedIncident = healthIncidentRepository.save(incident);
        log.info("Health incident ID: {} updated by user {}", updatedIncident.getIncidentId(), currentUser.getEmail());

        // Gửi thông báo cho phụ huynh
        sendIncidentUpdateNotification(updatedIncident);

        return healthIncidentMapper.toDto(updatedIncident);
    }

    private void sendIncidentUpdateNotification(HealthIncident incident) {
        try {
            String content = String.format("Thông tin sự cố sức khỏe của học sinh %s (xảy ra lúc %s) vừa được cập nhật.",
                    incident.getStudent().getFullName(), incident.getIncidentDateTime().toLocalDate());
            String link = "/health-incidents/" + incident.getIncidentId();
            // Người gửi là người cập nhật sự cố
            String sender = incident.getUpdatedByUser() != null ? incident.getUpdatedByUser().getEmail() : "system";

            sendNotificationToParents(incident.getStudent(), content, link, sender, "cập nhật sự cố");
        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo cập nhật sự cố sức khỏe ID {}: {}", incident.getIncidentId(), e.getMessage(), e);
        }
    }

    private void sendNotificationToParents(Student student, String content, String link, String sender, String logContext) {
        if (student == null || student.getParentLinks() == null || student.getParentLinks().isEmpty()) {
            log.warn("Không thể gửi thông báo {}. Không có thông tin phụ huynh cho học sinh ID: {}", logContext, student != null ? student.getId() : "null");
            return;
        }

        student.getParentLinks().forEach(parentLink -> {
            User parent = parentLink.getParent();
            if (parent != null && parent.getEmail() != null) {
                notificationService.createAndSendNotification(parent.getEmail(), content, link, sender);
                log.info("Đã yêu cầu gửi thông báo {} tới phụ huynh: {}", logContext, parent.getEmail());
            }
        });
    }

    @Transactional
    public void deleteHealthIncident(Long incidentId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("User {} is attempting to SOFT-DELETE health incident ID: {} AND process supply return.",
                currentUser.getEmail(), incidentId);

        // Bước 1: Lấy HealthIncident và các supply usages liên quan.
        // Sử dụng phương thức fetch join để đảm bảo supplyUsages được tải.
        HealthIncident incidentToSoftDelete = healthIncidentRepository
                .findIncidentEvenIfDeletedWithUsages(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Health incident not found with ID: " + incidentId + ". It may not exist."));

        // Bước 2: Kiểm tra xem nó đã bị xóa mềm trước đó chưa.
        if (incidentToSoftDelete.isDeleted()) {
            log.warn("User {} attempted to delete an already soft-deleted health incident ID: {}", currentUser.getEmail(), incidentId);
            throw new IllegalStateException("Health incident with ID " + incidentId + " is already deleted.");
        }

        // Bước 3: Kiểm tra quyền và điều kiện xóa (trong ngày, đúng vai trò)
        if (!incidentToSoftDelete.getCreatedAt().toLocalDate().isEqual(LocalDate.now())) {
            log.warn("User {} attempted to delete health incident ID {} which was not created today.",
                    currentUser.getEmail(), incidentId);
            throw new IllegalStateException("Cannot delete a health incident that was not created today. Deletion is only allowed on the day of creation.");
        }

        User recordedUser = incidentToSoftDelete.getRecordedByUser();
        boolean canDelete = false;
        if (currentUser.getRole() == UserRole.StaffManager || currentUser.getRole() == UserRole.SchoolAdmin) {
            canDelete = true;
        } else if (currentUser.getRole() == UserRole.MedicalStaff) {
            if (recordedUser != null && currentUser.getUserId().equals(recordedUser.getUserId())) {
                canDelete = true;
            }
        }

        if (!canDelete) {
            log.warn("User {} (Role: {}) attempted to delete health incident ID {} without permission.",
                    currentUser.getEmail(), currentUser.getRole(), incidentId);
            throw new AccessDeniedException("You do not have permission to delete this health incident. Only the creator, a Staff Manager, or a School Admin can delete it on the day of creation.");
        }

        // Bước 4: Xử lý hoàn trả vật tư và tạo transaction hoàn trả
        List<SupplyTransaction> originalUsages = new ArrayList<>(incidentToSoftDelete.getSupplyUsages());
        List<SupplyTransaction> returnTransactionsToSave = new ArrayList<>(); // Danh sách các transaction hoàn trả mới

        for (SupplyTransaction usage : originalUsages) {
            if (usage.getSupplyTransactionType() == SupplyTransactionType.USED_FOR_INCIDENT) {
                MedicalSupply supply = usage.getMedicalSupply();
                if (supply == null) {
                    log.warn("Original supply transaction ID {} for incident ID {} has no associated medical supply. Skipping return.",
                            usage.getTransactionId(), incidentId);
                    continue;
                }

                int quantityToReturn = usage.getQuantity();
                log.info("Processing (virtual) return of {} unit(s) of supply '{}' (ID: {}) due to soft delete of incident ID: {}.",
                        quantityToReturn, supply.getName(), supply.getSupplyId(), incidentId);

                // Tăng lại tồn kho
                int oldStock = supply.getCurrentStock();
                supply.setCurrentStock(oldStock + quantityToReturn);

                // Nếu trước đó là OUT_OF_STOCK và giờ có hàng, cập nhật trạng thái thành AVAILABLE
                if (oldStock == 0 && supply.getStatus() == MedicalSupplyStatus.OUT_OF_STOCK) {
                    supply.setStatus(MedicalSupplyStatus.AVAILABLE);
                    log.info("Medical supply ID {} now has stock again, updating status to AVAILABLE", supply.getSupplyId());
                }

                supply.setUpdatedByUser(currentUser);
                medicalSupplyRepository.save(supply); // Lưu thay đổi tồn kho

                // Tạo giao dịch hoàn trả mới
                SupplyTransaction returnTransaction = SupplyTransaction.builder()
                        .medicalSupply(supply)
                        .quantity(quantityToReturn)
                        .supplyTransactionType(SupplyTransactionType.RETURN_FROM_INCIDENT)
                        .note("Hoàn trả do xóa mềm sự cố ID: " + incidentId + ". Giao dịch xuất kho gốc ID: " + usage.getTransactionId())
                        .performedByUser(currentUser)
                        // transactionDateTime sẽ được @CreationTimestamp xử lý
                        .healthIncident(incidentToSoftDelete) // Liên kết với HealthIncident (dù nó sẽ bị soft delete)
                        .build();
                returnTransactionsToSave.add(returnTransaction);
            }
        }

        // Lưu tất cả các transaction hoàn trả mới
        if (!returnTransactionsToSave.isEmpty()) {
            supplyTransactionRepository.saveAll(returnTransactionsToSave);
            log.info("Saved {} return supply transactions for incident ID: {}", returnTransactionsToSave.size(), incidentId);
        }

        // Bước 5: Thực hiện soft delete cho HealthIncident
        incidentToSoftDelete.setDeleted(true);
        incidentToSoftDelete.setDeletedAt(LocalDateTime.now());
        incidentToSoftDelete.setDeletedByUser(currentUser);
        incidentToSoftDelete.setUpdatedByUser(currentUser);


        healthIncidentRepository.save(incidentToSoftDelete);

        log.info("Health incident ID: {} successfully SOFT-DELETED by user {}. Supplies virtually returned.",
                incidentId, currentUser.getEmail());
    }
}
