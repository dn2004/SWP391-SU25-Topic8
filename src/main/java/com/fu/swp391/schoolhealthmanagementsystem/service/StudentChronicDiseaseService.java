package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.ChronicDiseaseStatusUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.disease.StudentChronicDiseaseUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentChronicDisease;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentChronicDiseaseStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.FileStorageException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.InvalidOperationException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.StudentChronicDiseaseMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentChronicDiseaseRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.StudentChronicDiseaseSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentChronicDiseaseService {

    private final StudentChronicDiseaseRepository chronicDiseaseRepository;
    private final FileStorageService fileStorageService;
    private final StudentRepository studentRepository;
    private final StudentChronicDiseaseMapper chronicDiseaseMapper;
    private final AuthorizationService authorizationService;
    private final StudentChronicDiseaseSpecification chronicDiseaseSpecification;
    private final NotificationService notificationService;

    @Transactional
    public StudentChronicDiseaseResponseDto addChronicDisease(Long studentId, StudentChronicDiseaseRequestDto dto) {
        log.info("[CHRONIC] Bắt đầu thêm bệnh mãn tính cho học sinh ID: {}", studentId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        if (!isStudentActive(student)) {
            log.warn("[CHRONIC] Học sinh ID: {} không hoạt động, không thể thêm hồ sơ bệnh mãn tính", studentId);
            throw new InvalidOperationException("Học sinh không hoạt động, không thể thêm hồ sơ");
        }

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "thêm hồ sơ bệnh mãn tính");
        }

        StudentChronicDisease entity = chronicDiseaseMapper.toEntity(dto, student);
        entity.setCreatedByUser(currentUser);
        entity.setUpdatedByUser(currentUser);

        if (currentUser.getRole() == UserRole.Parent) {
            entity.setStatus(StudentChronicDiseaseStatus.PENDING);
        } else {
            entity.setStatus(StudentChronicDiseaseStatus.APPROVE);
            entity.setApprovedByUser(currentUser);
            entity.setApprovedAt(LocalDateTime.now());
        }

        MultipartFile attachmentFile = dto.attachmentFile();
        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            handleFileUpload(attachmentFile, studentId, entity);
        }

        StudentChronicDisease savedEntity = chronicDiseaseRepository.save(entity);
        log.info("[CHRONIC] Đã tạo thành công hồ sơ bệnh mãn tính ID: {} cho học sinh ID: {}", savedEntity.getId(), studentId);

        if (currentUser.getRole() == UserRole.Parent) {
            String content = String.format("Có hồ sơ bệnh mãn tính mới cho học sinh '%s' cần được duyệt.", student.getFullName());
            String link = "/admin/chronic-diseases/pending";
            notificationService.createAndSendNotificationToRole(UserRole.MedicalStaff, content, link, currentUser.getEmail());
            log.info("[CHRONIC] Đã gửi thông báo cho MedicalStaff về hồ sơ bệnh mãn tính mới của học sinh ID: {}", studentId);
        }

        return chronicDiseaseMapper.toDto(savedEntity);
    }

    private boolean isStudentActive(Student student) {
        return student.getStatus() == StudentStatus.ACTIVE;
    }

    @Transactional
    public StudentChronicDiseaseResponseDto updateChronicDiseaseForCurrentUser(Long chronicDiseaseId, StudentChronicDiseaseUpdateRequestDto dto) {
        log.info("[CHRONIC] Bắt đầu cập nhật hồ sơ bệnh mãn tính ID: {}", chronicDiseaseId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentChronicDisease existingEntity = chronicDiseaseRepository.findById(chronicDiseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh mãn tính với ID: " + chronicDiseaseId));

        Student student = existingEntity.getStudent();
        if (student == null) {
            throw new InvalidOperationException("Hồ sơ bệnh mãn tính không liên kết với học sinh nào.");
        }

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "cập nhật hồ sơ bệnh mãn tính");
            if (existingEntity.getStatus() != StudentChronicDiseaseStatus.PENDING) {
                log.warn("[CHRONIC] Phụ huynh cố gắng cập nhật hồ sơ ID: {} khi không ở trạng thái PENDING", chronicDiseaseId);
                throw new AccessDeniedException("Bạn chỉ có thể cập nhật hồ sơ khi đang ở trạng thái 'Chờ xử lý'.");
            }
        } else {
            if (existingEntity.getStatus() == StudentChronicDiseaseStatus.PENDING) {
                log.warn("[CHRONIC] Nhân viên cố gắng cập nhật hồ sơ ID: {} khi đang ở trạng thái PENDING", chronicDiseaseId);
                throw new AccessDeniedException("Nhân viên không thể cập nhật hồ sơ đang 'Chờ xử lý'. Vui lòng dùng chức năng duyệt.");
            }
        }

        chronicDiseaseMapper.updateEntityFromDto(dto, existingEntity);
        existingEntity.setUpdatedByUser(currentUser);

        MultipartFile attachmentFile = dto.attachmentFile();
        if (attachmentFile != null && !attachmentFile.isEmpty()) {
            handleFileUpload(attachmentFile, student.getId(), existingEntity);
        }

        if (currentUser.getRole() == UserRole.Parent) {
            existingEntity.setStatus(StudentChronicDiseaseStatus.PENDING);
            existingEntity.setApprovedByUser(null);
            existingEntity.setApprovedAt(null);
            existingEntity.setApproverNotes(null);
        } else {
            existingEntity.setStatus(StudentChronicDiseaseStatus.APPROVE);
            existingEntity.setApprovedByUser(currentUser);
            existingEntity.setApprovedAt(LocalDateTime.now());
            existingEntity.setApproverNotes("Cập nhật và duyệt tự động bởi nhân viên.");
        }

        StudentChronicDisease updatedEntity = chronicDiseaseRepository.save(existingEntity);
        log.info("[CHRONIC] Đã cập nhật thành công hồ sơ bệnh mãn tính ID: {}", updatedEntity.getId());

        if (currentUser.getRole() == UserRole.Parent) {
            String content = String.format("Phụ huynh vừa cập nhật hồ sơ bệnh mãn tính cho học sinh '%s'. Hồ sơ cần được duyệt lại.", student.getFullName());
            String link = "/admin/chronic-diseases/pending";
            notificationService.createAndSendNotificationToRole(UserRole.MedicalStaff, content, link, currentUser.getEmail());
            log.info("[CHRONIC] Đã gửi thông báo cho MedicalStaff về việc cập nhật hồ sơ bệnh mãn tính của học sinh ID: {}", student.getId());
        } else if (currentUser.getRole() == UserRole.MedicalStaff) {
            String content = String.format("Hồ sơ bệnh mãn tính của học sinh '%s' vừa được cập nhật và duyệt bởi nhân viên y tế.", student.getFullName());
            String link = "/chronic-diseases/" + updatedEntity.getId();
            if (student.getParentLinks() != null) {
                student.getParentLinks().forEach(parentLink -> {
                    User parent = parentLink.getParent();
                    if (parent != null && parent.getEmail() != null) {
                        notificationService.createAndSendNotification(parent.getEmail(), content, link, currentUser.getEmail());
                        log.info("[CHRONIC] Đã gửi thông báo cập nhật hồ sơ bệnh mãn tính ID {} tới phụ huynh: {}", updatedEntity.getId(), parent.getEmail());
                    }
                });
            }
        }

        return chronicDiseaseMapper.toDto(updatedEntity);
    }

    @Transactional
    public StudentChronicDiseaseResponseDto mediateChronicDiseaseStatus(Long chronicDiseaseId, ChronicDiseaseStatusUpdateRequestDto dto) {
        log.info("[CHRONIC] Bắt đầu duyệt hồ sơ bệnh mãn tính ID: {}", chronicDiseaseId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentChronicDisease entity = chronicDiseaseRepository.findById(chronicDiseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh mãn tính với ID: " + chronicDiseaseId));

        if (entity.getStatus() != StudentChronicDiseaseStatus.PENDING) {
            log.warn("[CHRONIC] Cố gắng duyệt hồ sơ ID: {} khi không ở trạng thái PENDING", chronicDiseaseId);
            throw new IllegalArgumentException("Chỉ có thể duyệt các hồ sơ đang ở trạng thái 'Chờ xử lý'.");
        }

        StudentChronicDiseaseStatus newStatus = dto.newStatus();
        if (newStatus != StudentChronicDiseaseStatus.APPROVE && newStatus != StudentChronicDiseaseStatus.REJECTED) {
            log.warn("[CHRONIC] Trạng thái duyệt không hợp lệ cho hồ sơ ID: {}: {}", chronicDiseaseId, newStatus);
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ. Chỉ chấp nhận APPROVE hoặc REJECTED.");
        }

        entity.setStatus(newStatus);
        entity.setApprovedByUser(currentUser);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setApproverNotes(dto.approverNotes());
        entity.setUpdatedByUser(currentUser);

        StudentChronicDisease updatedEntity = chronicDiseaseRepository.save(entity);
        log.info("[CHRONIC] Đã duyệt hồ sơ bệnh mãn tính ID: {}, trạng thái mới: {}", updatedEntity.getId(), updatedEntity.getStatus());

        sendMediationNotificationToParent(updatedEntity, dto.approverNotes());

        return chronicDiseaseMapper.toDto(updatedEntity);
    }

    @Transactional
    public void deleteChronicDiseaseForCurrentUser(Long chronicDiseaseId) {
        log.info("[CHRONIC] Bắt đầu xóa hồ sơ bệnh mãn tính ID: {}", chronicDiseaseId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentChronicDisease entity = chronicDiseaseRepository.findById(chronicDiseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh mãn tính với ID: " + chronicDiseaseId));

        Student student = entity.getStudent();
        if (student == null) {
            throw new InvalidOperationException("Hồ sơ không liên kết với học sinh.");
        }

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xóa hồ sơ bệnh mãn tính");
            if (entity.getStatus() != StudentChronicDiseaseStatus.PENDING) {
                log.warn("[CHRONIC] Phụ huynh cố gắng xóa hồ sơ ID: {} khi không ở trạng thái PENDING", chronicDiseaseId);
                throw new AccessDeniedException("Bạn chỉ có thể xóa hồ sơ đang ở trạng thái 'Chờ xử lý'.");
            }
        }

        if (entity.getAttachmentPublicId() != null && !entity.getAttachmentPublicId().isBlank()) {
            try {
                fileStorageService.deleteFile(entity.getAttachmentPublicId(), entity.getAttachmentResourceType());
            } catch (Exception e) {
                log.error("[CHRONIC] Lỗi xóa file đính kèm từ Cloudinary cho hồ sơ ID {}: {}", chronicDiseaseId, e.getMessage());
            }
        }

        chronicDiseaseRepository.delete(entity);

        if (student.getParentLinks() != null && !student.getParentLinks().isEmpty()) {
            String content = String.format("Hồ sơ bệnh mãn tính '%s' của học sinh %s đã được xóa khỏi hệ thống.",
                    entity.getDiseaseName(), student.getFullName());
            String link = "/chronic-diseases";
            String sender = currentUser.getEmail();
            student.getParentLinks().forEach(parentLink -> {
                User parent = parentLink.getParent();
                if (parent != null && parent.getEmail() != null) {
                    notificationService.createAndSendNotification(parent.getEmail(), content, link, sender);
                    log.info("[CHRONIC] Đã gửi thông báo xóa hồ sơ bệnh mãn tính ID {} tới phụ huynh: {}", chronicDiseaseId, parent.getEmail());
                }
            });
        }

        log.info("[CHRONIC] Đã xóa thành công hồ sơ bệnh mãn tính ID: {}", chronicDiseaseId);
    }

    @Transactional(readOnly = true)
    public StudentChronicDiseaseResponseDto getChronicDiseaseById(Long chronicDiseaseId) {
        log.info("[CHRONIC] Lấy thông tin hồ sơ bệnh mãn tính ID: {}", chronicDiseaseId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentChronicDisease entity = chronicDiseaseRepository.findById(chronicDiseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh mãn tính với ID: " + chronicDiseaseId));

        Student student = entity.getStudent();
        if (student == null) {
            throw new InvalidOperationException("Hồ sơ không liên kết với học sinh.");
        }

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem thông tin bệnh mãn tính");
        }

        return chronicDiseaseMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public Page<StudentChronicDiseaseResponseDto> getAllChronicDiseases(String studentName, String diseaseName, StudentChronicDiseaseStatus status, Pageable pageable) {
        log.info("[CHRONIC] Lấy danh sách tất cả bệnh mãn tính với bộ lọc: studentName='{}', diseaseName='{}', status='{}'", studentName, diseaseName, status);
        Specification<StudentChronicDisease> spec = Specification.allOf(
                chronicDiseaseSpecification.hasStudentNameContaining(studentName),
                chronicDiseaseSpecification.hasDiseaseNameContaining(diseaseName),
                chronicDiseaseSpecification.hasStatus(status)
        );
        Page<StudentChronicDisease> page = chronicDiseaseRepository.findAll(spec, pageable);
        log.info("[CHRONIC] Đã trả về {} hồ sơ bệnh mãn tính theo tiêu chí tìm kiếm.", page.getTotalElements());
        return page.map(chronicDiseaseMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StudentChronicDiseaseResponseDto> getAllChronicDiseasesByStudentId(Long studentId, String diseaseName, StudentChronicDiseaseStatus status, Pageable pageable) {
        log.info("[CHRONIC] Lấy danh sách bệnh mãn tính cho học sinh ID: {} với bộ lọc diseaseName='{}', status='{}'", studentId, diseaseName, status);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem danh sách bệnh mãn tính");
        }

        Specification<StudentChronicDisease> spec = Specification.allOf(
                chronicDiseaseSpecification.forStudent(studentId),
                chronicDiseaseSpecification.hasDiseaseNameContaining(diseaseName),
                chronicDiseaseSpecification.hasStatus(status)
        );
        Page<StudentChronicDisease> page = chronicDiseaseRepository.findAll(spec, pageable);
        log.info("[CHRONIC] Đã trả về {} hồ sơ bệnh mãn tính cho học sinh ID: {}.", page.getTotalElements(), studentId);
        return page.map(chronicDiseaseMapper::toDto);
    }

    @Transactional(readOnly = true)
    public String getSignedUrlForAttachment(Long chronicDiseaseId) {
        log.info("[CHRONIC] Yêu cầu URL đã ký cho file đính kèm của hồ sơ bệnh mãn tính ID: {}", chronicDiseaseId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentChronicDisease entity = chronicDiseaseRepository.findById(chronicDiseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ bệnh mãn tính với ID: " + chronicDiseaseId));

        Student student = entity.getStudent();
        if (student == null) {
            throw new InvalidOperationException("Hồ sơ không liên kết với học sinh.");
        }

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "truy cập file đính kèm");
        }

        if (entity.getAttachmentPublicId() == null || entity.getAttachmentPublicId().isBlank()) {
            log.warn("[CHRONIC] Hồ sơ bệnh mãn tính ID: {} không có file đính kèm.", chronicDiseaseId);
            return null;
        }

        try {
            int urlDurationSeconds = 300;
            String signedUrl = fileStorageService.generateSignedUrl(entity.getAttachmentPublicId(), entity.getAttachmentResourceType(), urlDurationSeconds);
            log.info("[CHRONIC] Đã tạo signed URL cho file đính kèm hồ sơ ID: {}", chronicDiseaseId);
            return signedUrl;
        } catch (Exception e) {
            log.error("[CHRONIC] Không thể tạo URL đã ký cho file đính kèm của hồ sơ ID {}: {}", chronicDiseaseId, e.getMessage(), e);
            throw new FileStorageException("Lỗi khi tạo URL truy cập file.", e);
        }
    }

    private void handleFileUpload(MultipartFile file, Long studentId, StudentChronicDisease entity) {
        if (entity.getAttachmentPublicId() != null && !entity.getAttachmentPublicId().isBlank()) {
            try {
                fileStorageService.deleteFile(entity.getAttachmentPublicId(), entity.getAttachmentResourceType());
            } catch (Exception e) {
                log.error("[CHRONIC] Lỗi xóa file đính kèm cũ từ Cloudinary: {}", e.getMessage());
            }
        }
        try {
            String folderName = String.format("chronic_diseases/student_%d", studentId);
            String publicIdPrefix = String.format("student_%d_chronic_disease", studentId);
            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(file, folderName, publicIdPrefix);
            chronicDiseaseMapper.updateAttachmentFileDetailsFromUploadResult(uploadResult, entity);
            entity.setAttachmentFileOriginalName(file.getOriginalFilename());
            entity.setAttachmentFileType(file.getContentType());
            log.info("[CHRONIC] Đã upload file đính kèm mới cho học sinh ID: {}", studentId);
        } catch (Exception e) {
            log.error("[CHRONIC] Lỗi tải file đính kèm mới cho học sinh ID {}: {}", studentId, e.getMessage(), e);
            throw new FileStorageException("Lỗi tải file đính kèm: " + e.getMessage(), e);
        }
    }

    private void sendMediationNotificationToParent(StudentChronicDisease chronicDisease, String reason) {
        try {
            Student student = chronicDisease.getStudent();
            if (student == null || student.getParentLinks() == null || student.getParentLinks().isEmpty()) {
                log.warn("[CHRONIC] Không thể gửi thông báo duyệt. Không có thông tin phụ huynh cho học sinh ID: {}", student != null ? student.getId() : "null");
                return;
            }

            String statusMessage = switch (chronicDisease.getStatus()) {
                case APPROVE -> "đã được duyệt";
                case REJECTED -> "đã bị từ chối với lí do " + reason + ".";
                default -> null;
            };

            if (statusMessage != null) {
                String content = String.format("Hồ sơ bệnh mãn tính '%s' của học sinh %s %s.",
                        chronicDisease.getDiseaseName(), student.getFullName(), statusMessage);
                String link = "/chronic-diseases/" + chronicDisease.getId();
                String sender = chronicDisease.getApprovedByUser() != null ? chronicDisease.getApprovedByUser().getEmail() : "system";

                student.getParentLinks().forEach(parentLink -> {
                    User parent = parentLink.getParent();
                    if (parent != null && parent.getEmail() != null) {
                        notificationService.createAndSendNotification(parent.getEmail(), content, link, sender);
                        log.info("[CHRONIC] Đã yêu cầu gửi thông báo duyệt hồ sơ bệnh mãn tính ID {} tới phụ huynh: {}", chronicDisease.getId(), parent.getEmail());
                    }
                });
            }
        } catch (Exception e) {
            log.error("[CHRONIC] Lỗi khi gửi thông báo duyệt hồ sơ bệnh mãn tính ID {}: {}", chronicDisease.getId(), e.getMessage(), e);
        }
    }
}
