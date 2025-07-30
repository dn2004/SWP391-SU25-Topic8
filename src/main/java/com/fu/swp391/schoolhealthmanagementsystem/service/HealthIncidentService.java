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
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
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
    private final HealthIncidentMapper healthIncidentMapper;
    private final AuthorizationService authorizationService;
    private final SupplyTransactionRepository supplyTransactionRepository;
    private final HealthIncidentSpecification healthIncidentSpecification;
    private final NotificationService notificationService; // Inject NotificationService


    // --- TẠO MỚI ---
    @Transactional
    public HealthIncidentResponseDto createHealthIncident(CreateHealthIncidentRequestDto requestDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[SỰ CỐ] Người dùng {} đang tạo sự cố sức khỏe cho học sinh ID: {}", currentUser.getEmail(), requestDto.studentId());

        Student student = studentRepository.findById(requestDto.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + requestDto.studentId()));

        HealthIncident incident = healthIncidentMapper.toEntity(requestDto);
        incident.setStudent(student);
        incident.setRecordedByUser(currentUser); // Gán người tạo
        // incident.setDeleted(false); // Mapper đã set hoặc @Builder.Default

        // Kiểm tra trước số lượng vật tư y tế còn đủ không
        if (requestDto.supplyUsages() != null && !requestDto.supplyUsages().isEmpty()) {
            for (HealthIncidentSupplyUsageDto usageDto : requestDto.supplyUsages()) {
                MedicalSupply supply = medicalSupplyRepository.findById(usageDto.supplyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + usageDto.supplyId()));

                if (supply.getStatus() == MedicalSupplyStatus.DISPOSE || supply.getStatus() == MedicalSupplyStatus.EXPIRED) {
                    throw new InvalidOperationException("Vật tư y tế '" + supply.getName() + "' (ID: " + supply.getSupplyId() +
                            ") đang ở trạng thái " + supply.getStatus() + " và không thể sử dụng.");
                }

                if (supply.getCurrentStock() < usageDto.quantityUsed()) {
                    throw new InvalidOperationException("Không đủ tồn kho cho vật tư y tế '" + supply.getName() + "'. Yêu cầu: " +
                            usageDto.quantityUsed() + ", Hiện có: " + supply.getCurrentStock());
                }
            }
        }

        HealthIncident savedIncident = healthIncidentRepository.save(incident);

        // Đảm bảo lưu các SupplyTransaction trước khi liên kết với HealthIncident
        if (requestDto.supplyUsages() != null && !requestDto.supplyUsages().isEmpty()) {
            for (HealthIncidentSupplyUsageDto usageDto : requestDto.supplyUsages()) {
                MedicalSupply supply = medicalSupplyRepository.findById(usageDto.supplyId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vật tư y tế với ID: " + usageDto.supplyId()));

                if (supply.getStatus() == MedicalSupplyStatus.DISPOSE || supply.getStatus() == MedicalSupplyStatus.EXPIRED) {
                    throw new InvalidOperationException("Vật tư y tế '" + supply.getName() + "' (ID: " + supply.getSupplyId() +
                            ") đang ở trạng thái " + supply.getStatus() + " và không thể sử dụng.");
                }

                if (supply.getCurrentStock() < usageDto.quantityUsed()) {
                    throw new InvalidOperationException("Không đủ tồn kho cho vật tư y tế '" + supply.getName() + "'. Yêu cầu: " +
                            usageDto.quantityUsed() + ", Hiện có: " + supply.getCurrentStock());
                }

                // Tạo và lưu giao dịch xuất kho trước khi liên kết
                SupplyTransaction transaction = new SupplyTransaction();
                transaction.setMedicalSupply(supply);
                transaction.setQuantity(usageDto.quantityUsed());
                transaction.setSupplyTransactionType(SupplyTransactionType.USED_FOR_INCIDENT);
                transaction.setHealthIncident(savedIncident);
                transaction.setPerformedByUser(currentUser);
                supplyTransactionRepository.save(transaction);

                // Cập nhật tồn kho vật tư
                int oldStock = supply.getCurrentStock();
                supply.setCurrentStock(oldStock - usageDto.quantityUsed());

                // Nếu hết hàng thì cập nhật trạng thái
                if (supply.getCurrentStock() == 0) {
                    supply.setStatus(MedicalSupplyStatus.OUT_OF_STOCK);
                    log.info("[SỰ CỐ] Vật tư y tế ID {} đã hết hàng, cập nhật trạng thái OUT_OF_STOCK", supply.getSupplyId());
                }

                medicalSupplyRepository.save(supply);
            }
        }

        // Lấy lại sự cố đã lưu để trả về đầy đủ thông tin
        HealthIncident finalIncidentWithTransactions = healthIncidentRepository.findById(savedIncident.getIncidentId())
                .orElseThrow(() -> new InvalidOperationException("Không thể lấy lại sự cố vừa tạo: " + savedIncident.getIncidentId()));

        log.info("[SỰ CỐ] Đã tạo sự cố ID: {} cho học sinh {}, người ghi nhận: {}",
                finalIncidentWithTransactions.getIncidentId(), student.getFullName(), currentUser.getFullName());

        // Gửi thông báo cho phụ huynh
        sendIncidentCreationNotification(finalIncidentWithTransactions);

        return healthIncidentMapper.toDto(finalIncidentWithTransactions);
    }

    /**
     * Gửi thông báo cho phụ huynh khi tạo mới sự cố sức khỏe
     */
    private void sendIncidentCreationNotification(HealthIncident incident) {
        try {
            String content = String.format("Một sự cố sức khỏe vừa được ghi nhận cho học sinh '%s'.",
                    incident.getStudent().getFullName());
            String link = "/health-incidents/" + incident.getIncidentId();
            // Người gửi là người ghi nhận sự cố
            String sender = incident.getRecordedByUser() != null ? incident.getRecordedByUser().getEmail() : "system";

            sendNotificationToParents(incident.getStudent(), content, link, sender, "tạo mới sự cố");
        } catch (Exception e) {
            log.error("[SỰ CỐ] Lỗi khi gửi thông báo tạo mới sự cố sức khỏe ID {}: {}", incident.getIncidentId(), e.getMessage(), e);
        }
    }

    // --- READ ---
    @Transactional(readOnly = true)
    public HealthIncidentResponseDto getHealthIncidentById(Long incidentId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        HealthIncident incident = healthIncidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố sức khỏe với ID: " + incidentId));

        Student studentOfIncident = incident.getStudent();
        if (studentOfIncident == null) {
            // Trường hợp này không nên xảy ra nếu ràng buộc DB đúng
            log.error("[SỰ CỐ] Sự cố sức khỏe ID {} không liên kết với học sinh nào.", incidentId);
            throw new InvalidOperationException("Dữ liệu sự cố sức khỏe bị lỗi: không có học sinh liên kết.");
        }

        // Kiểm tra quyền truy cập
        if (currentUser.getRole() == UserRole.Parent) {
            // Sử dụng hàm đã có trong AuthorizationService
            authorizationService.authorizeParentAction(currentUser, studentOfIncident, "xem chi tiết sự cố sức khỏe");
        } else if (!(currentUser.getRole() == UserRole.MedicalStaff ||
                currentUser.getRole() == UserRole.StaffManager ||
                currentUser.getRole() == UserRole.SchoolAdmin)) {
            log.warn("[SỰ CỐ] Người dùng {} với vai trò {} cố truy cập sự cố sức khỏe {} mà không đủ quyền.",
                    currentUser.getEmail(), currentUser.getRole(), incidentId);
            throw new AccessDeniedException("Bạn không có quyền xem sự cố sức khỏe này.");
        }
        log.info("[SỰ CỐ] Người dùng {} đã lấy thông tin sự cố sức khỏe ID: {}", currentUser.getEmail(), incidentId);
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        if (currentUser.getRole() == UserRole.Parent) {
            // Sử dụng hàm đã có trong AuthorizationService
            authorizationService.authorizeParentAction(currentUser, student, "xem danh sách sự cố sức khỏe");
        } else if (!(currentUser.getRole() == UserRole.MedicalStaff ||
                currentUser.getRole() == UserRole.StaffManager ||
                currentUser.getRole() == UserRole.SchoolAdmin)) {
            log.warn("[SỰ CỐ] Người dùng {} với vai trò {} cố truy cập danh sách sự cố sức khỏe của học sinh {} mà không đủ quyền.",
                    currentUser.getEmail(), currentUser.getRole(), studentId);
            throw new AccessDeniedException("Bạn không có quyền xem danh sách sự cố sức khỏe của học sinh này.");
        }

        log.info("[SỰ CỐ] Người dùng {} lấy danh sách sự cố sức khỏe cho học sinh ID: {} với bộ lọc - Loại: {}, Địa điểm: {}, Từ ngày: {}, Đến ngày: {}",
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
        // Phân quyền cho các vai trò này sẽ được xử lý ở controller bằng @PreAuthorize
        // e.g., @PreAuthorize("hasAnyRole('MedicalStaff', 'StaffManager', 'SchoolAdmin')")

        log.info("[SỰ CỐ] Người dùng {} lấy tất cả sự cố sức khỏe với bộ lọc - Loại: {}, Từ ngày: {}, Đến ngày: {}, Tên học sinh: {}, Người ghi nhận: {}, Địa điểm: {}, Mô tả: {}",
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
        // @PreAuthorize("hasRole('MedicalStaff')") ở controller sẽ xử lý phân quyền

        log.info("[SỰ CỐ] Người dùng {} lấy danh sách sự cố sức khỏe do mình ghi nhận với bộ lọc - Loại: {}, Từ ngày: {}, Đến ngày: {}, Tên học sinh: {}, Địa điểm: {}, Mô tả: {}",
                currentUser.getEmail(), incidentType, startDate, endDate, studentName, location, description);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59, 999999999) : null;

        Specification<HealthIncident> spec = Specification
                .allOf(healthIncidentSpecification.isNotDeleted())
                .and(healthIncidentSpecification.recordedBy(currentUser.getUserId())) // Lọc theo user hiện tại
                .and(healthIncidentSpecification.forStudentName(studentName))
                .and(healthIncidentSpecification.hasType(incidentType))
                .and(healthIncidentSpecification.happenedOnOrAfter(startDateTime))
                .and(healthIncidentSpecification.happenedOnOrBefore(endDateTime))
                .and(healthIncidentSpecification.hasLocationContaining(location))
                .and(healthIncidentSpecification.descriptionContaining(description));


        return healthIncidentRepository.findAll(spec, pageable)
                .map(healthIncidentMapper::toDto);
    }

    // --- CẬP NHẬT ---
    @Transactional
    public HealthIncidentResponseDto updateHealthIncident(Long incidentId, UpdateHealthIncidentRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        HealthIncident incident = healthIncidentRepository.findById(incidentId) // Sẽ không tìm thấy nếu is_deleted = true
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố sức khỏe với ID: " + incidentId));

        User recordedUser = incident.getRecordedByUser();

        // Chỉ cho phép cập nhật trong vòng 1 ngày kể từ khi tạo
        if (incident.getCreatedAt().isBefore(LocalDateTime.now().minusDays(1))) {
            log.warn("[SỰ CỐ] Người dùng {} cố cập nhật sự cố ID {} đã tạo quá 1 ngày.",
                    currentUser.getEmail(), incidentId);
            throw new InvalidOperationException("Không thể cập nhật sự cố đã tạo quá 1 ngày.");
        }

        // Chỉ người ghi nhận hoặc quản trị viên mới được cập nhật
        boolean canUpdate = false;
        if (currentUser.getRole() == UserRole.StaffManager || currentUser.getRole() == UserRole.SchoolAdmin) {
            canUpdate = true;
        } else if (currentUser.getRole() == UserRole.MedicalStaff) {
            if (currentUser.getUserId().equals(recordedUser.getUserId())) {
                canUpdate = true;
            }
        }

        if (!canUpdate) {
            log.warn("[SỰ CỐ] Người dùng {} (Vai trò: {}) cố cập nhật sự cố ID {} mà không đủ quyền.",
                    currentUser.getEmail(), currentUser.getRole(), incidentId);
            throw new AccessDeniedException("Bạn không có quyền cập nhật sự cố sức khỏe này.");
        }

        healthIncidentMapper.updateEntityFromDto(updateDto, incident);
        incident.setUpdatedByUser(currentUser);

        HealthIncident updatedIncident = healthIncidentRepository.save(incident);
        log.info("[SỰ CỐ] Đã cập nhật sự cố ID: {} bởi người dùng {}", updatedIncident.getIncidentId(), currentUser.getEmail());

        // Gửi thông báo cho phụ huynh
        sendIncidentUpdateNotification(updatedIncident);

        return healthIncidentMapper.toDto(updatedIncident);
    }

    /**
     * Gửi thông báo cho phụ huynh khi cập nhật sự cố sức khỏe
     */
    private void sendIncidentUpdateNotification(HealthIncident incident) {
        try {
            String content = String.format("Thông tin sự cố sức khỏe của học sinh %s (xảy ra lúc %s) vừa được cập nhật.",
                    incident.getStudent().getFullName(), incident.getIncidentDateTime().toLocalDate());
            String link = "/health-incidents/" + incident.getIncidentId();
            // Người gửi là người cập nhật sự cố
            String sender = incident.getUpdatedByUser() != null ? incident.getUpdatedByUser().getEmail() : "system";

            sendNotificationToParents(incident.getStudent(), content, link, sender, "cập nhật sự cố");
        } catch (Exception e) {
            log.error("[SỰ CỐ] Lỗi khi gửi thông báo cập nhật sự cố sức khỏe ID {}: {}", incident.getIncidentId(), e.getMessage(), e);
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
        log.info("[SỰ CỐ] Người dùng {} đang thực hiện xóa mềm sự cố sức khỏe ID: {} và hoàn trả vật tư.",
                currentUser.getEmail(), incidentId);

        // Bước 1: Lấy HealthIncident và các supply usages liên quan.
        HealthIncident incidentToSoftDelete = healthIncidentRepository
                .findIncidentEvenIfDeletedWithUsages(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự cố sức khỏe với ID: " + incidentId + ". Có thể không tồn tại."));

        // Bước 2: Kiểm tra đã xóa mềm chưa
        if (incidentToSoftDelete.isDeleted()) {
            log.warn("[SỰ CỐ] Người dùng {} cố xóa sự cố đã bị xóa mềm trước đó ID: {}", currentUser.getEmail(), incidentId);
            throw new InvalidOperationException("Sự cố sức khỏe với ID " + incidentId + " đã bị xóa trước đó.");
        }

        // Bước 3: Chỉ cho phép xóa trong ngày tạo và đúng vai trò
        if (!incidentToSoftDelete.getCreatedAt().toLocalDate().isEqual(LocalDate.now())) {
            log.warn("[SỰ CỐ] Người dùng {} cố xóa sự cố ID {} không được tạo trong ngày.",
                    currentUser.getEmail(), incidentId);
            throw new InvalidOperationException("Chỉ được phép xóa sự cố trong ngày tạo.");
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
            log.warn("[SỰ CỐ] Người dùng {} (Vai trò: {}) cố xóa sự cố ID {} mà không đủ quyền.",
                    currentUser.getEmail(), currentUser.getRole(), incidentId);
            throw new AccessDeniedException("Bạn không có quyền xóa sự cố sức khỏe này. Chỉ người tạo, Quản lý hoặc Quản trị viên mới được xóa trong ngày tạo.");
        }

        // Bước 4: Hoàn trả vật tư và tạo transaction hoàn trả
        List<SupplyTransaction> originalUsages = new ArrayList<>(incidentToSoftDelete.getSupplyUsages());
        List<SupplyTransaction> returnTransactionsToSave = new ArrayList<>();

        for (SupplyTransaction usage : originalUsages) {
            if (usage.getSupplyTransactionType() == SupplyTransactionType.USED_FOR_INCIDENT) {
                MedicalSupply supply = usage.getMedicalSupply();
                if (supply == null) {
                    log.warn("[SỰ CỐ] Giao dịch vật tư gốc ID {} của sự cố ID {} không có vật tư liên kết. Bỏ qua hoàn trả.",
                            usage.getTransactionId(), incidentId);
                    continue;
                }

                int quantityToReturn = usage.getQuantity();
                log.info("[SỰ CỐ] Hoàn trả (ảo) {} đơn vị vật tư '{}' (ID: {}) do xóa mềm sự cố ID: {}.",
                        quantityToReturn, supply.getName(), supply.getSupplyId(), incidentId);

                // Tăng lại tồn kho
                int oldStock = supply.getCurrentStock();
                supply.setCurrentStock(oldStock + quantityToReturn);

                // Nếu trước đó là OUT_OF_STOCK và giờ có hàng, cập nhật trạng thái thành AVAILABLE
                if (oldStock == 0 && supply.getStatus() == MedicalSupplyStatus.OUT_OF_STOCK) {
                    supply.setStatus(MedicalSupplyStatus.AVAILABLE);
                    log.info("[SỰ CỐ] Vật tư y tế ID {} đã có hàng trở lại, cập nhật trạng thái AVAILABLE", supply.getSupplyId());
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
                        .healthIncident(incidentToSoftDelete)
                        .build();
                returnTransactionsToSave.add(returnTransaction);
            }
        }

        // Lưu tất cả các transaction hoàn trả mới
        if (!returnTransactionsToSave.isEmpty()) {
            supplyTransactionRepository.saveAll(returnTransactionsToSave);
            log.info("[SỰ CỐ] Đã lưu {} giao dịch hoàn trả vật tư cho sự cố ID: {}", returnTransactionsToSave.size(), incidentId);
        }

        // Bước 5: Thực hiện soft delete cho HealthIncident
        incidentToSoftDelete.setDeleted(true);
        incidentToSoftDelete.setDeletedAt(LocalDateTime.now());
        incidentToSoftDelete.setDeletedByUser(currentUser);
        incidentToSoftDelete.setUpdatedByUser(currentUser);

        healthIncidentRepository.save(incidentToSoftDelete);

        log.info("[SỰ CỐ] Đã xóa mềm sự cố ID: {} thành công bởi người dùng {}. Vật tư đã được hoàn trả (ảo).",
                incidentId, currentUser.getEmail());
    }
}
