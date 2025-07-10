package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.vaccination.VaccinationStatusUpdateRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.StudentVaccinationStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.FileStorageException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.StudentVaccinationMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentVaccinationRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.StudentVaccinationSpecification;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentVaccinationService {

    private final StudentVaccinationRepository vaccinationRepository;
    private final FileStorageService fileStorageService;
    private final StudentRepository studentRepository;
    private final StudentVaccinationMapper vaccinationMapper;
    private final AuthorizationService authorizationService;
    private final StudentVaccinationSpecification vaccinationSpecification;
    private final NotificationService notificationService;

    // Phương thức này vẫn giữ studentId vì nó tạo mới cho một student cụ thể
    @Transactional
    public StudentVaccinationResponseDto addVaccination(Long studentId, StudentVaccinationRequestDto dto) {
        log.info("Bắt đầu thêm thông tin tiêm chủng cho học sinh ID: {}", studentId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        StudentVaccinationStatus vaccinationStatus = StudentVaccinationStatus.PENDING;
        User approvedByUser = null; // Mặc định không có người duyệt

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "thêm thông tin tiêm chủng");
        } else {
            vaccinationStatus = StudentVaccinationStatus.APPROVE;
            approvedByUser = currentUser; // Nếu là nhân viên/admin, gán người duyệt là người hiện tại
        }

        StudentVaccination vaccinationEntity = vaccinationMapper.requestDtoToEntity(dto);
        vaccinationEntity.setStudent(student);
        vaccinationEntity.setStatus(vaccinationStatus);
        vaccinationEntity.setCreatedByUser(currentUser); // Gán người tạo
        vaccinationEntity.setApprovedByUser(approvedByUser);

        log.info("Gán trạng thái mặc định PENDING cho bản ghi tiêm chủng mới của học sinh ID: {}", studentId);

        MultipartFile proofFile = dto.proofFile();
        if (proofFile != null && !proofFile.isEmpty()) {
            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(proofFile, "vaccinations/student_" + studentId, "student_" + studentId + "_vacc_proof");
            vaccinationMapper.updateProofFileDetailsFromUploadResult(uploadResult, vaccinationEntity);
        }

        StudentVaccination savedEntity = vaccinationRepository.save(vaccinationEntity);
        log.info("Đã lưu bản ghi tiêm chủng với ID: {} và trạng thái: {}", savedEntity.getStudentVaccinationId(), savedEntity.getStatus());

        // Gửi thông báo cho MedicalStaff nếu người tạo là phụ huynh
        if (currentUser.getRole() == UserRole.Parent) {
            String content = String.format("Có hồ sơ tiêm chủng mới cho học sinh '%s' cần được duyệt.", student.getFullName());
            String link = "/admin/vaccinations/pending"; // Link tới trang duyệt của admin/staff
            notificationService.createAndSendNotificationToRole(UserRole.MedicalStaff, content, link, currentUser.getEmail());
            log.info("Đã gửi thông báo cho MedicalStaff về hồ sơ tiêm chủng mới của học sinh ID: {}", studentId);
        }

        return vaccinationMapper.toDto(savedEntity);
    }

    @Transactional(readOnly = true)
    public Page<StudentVaccinationResponseDto> getAllVaccinations(String studentName, String vaccineName, LocalDate fromDate, LocalDate toDate, StudentVaccinationStatus status, Pageable pageable) {
        log.info("Lấy tất cả danh sách tiêm chủng phân trang với bộ lọc.");

        Specification<StudentVaccination> spec = Specification.allOf(vaccinationSpecification.hasStudentNameContaining(studentName))
                .and(vaccinationSpecification.hasVaccineNameContaining(vaccineName))
                .and(vaccinationSpecification.vaccinatedOnOrAfter(fromDate))
                .and(vaccinationSpecification.vaccinatedOnOrBefore(toDate))
                .and(vaccinationSpecification.hasStatus(status));

        Page<StudentVaccination> vaccinationsEntityPage = vaccinationRepository.findAll(spec, pageable);
        log.info("Tìm thấy {} bản ghi tiêm chủng trên trang {}.", vaccinationsEntityPage.getNumberOfElements(), pageable.getPageNumber());
        return vaccinationsEntityPage.map(vaccinationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StudentVaccinationResponseDto> getAllVaccinationsByStudentIdPage(Long studentId, String vaccineName, LocalDate fromDate, LocalDate toDate, StudentVaccinationStatus status, Pageable pageable) {
        log.info("Lấy danh sách tiêm chủng phân trang cho học sinh ID: {}. Trang: {}", studentId, pageable.getPageNumber());
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem danh sách tiêm chủng");
        }

        Specification<StudentVaccination> spec = Specification.allOf(vaccinationSpecification.forStudent(studentId))
                .and(vaccinationSpecification.hasVaccineNameContaining(vaccineName))
                .and(vaccinationSpecification.vaccinatedOnOrAfter(fromDate))
                .and(vaccinationSpecification.vaccinatedOnOrBefore(toDate))
                .and(vaccinationSpecification.hasStatus(status));


        Page<StudentVaccination> vaccinationsEntityPage = vaccinationRepository.findAll(spec, pageable);
        log.info("Tìm thấy {} bản ghi tiêm chủng cho học sinh ID {} trên trang {}.", vaccinationsEntityPage.getNumberOfElements(), studentId, pageable.getPageNumber());
        return vaccinationsEntityPage.map(vaccinationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StudentVaccinationResponseDto> getPendingVaccinations(Pageable pageable) {
        log.info("Lấy danh sách tiêm chủng đang chờ duyệt (PENDING) cho trang: {}. Sắp xếp: {}", pageable.getPageNumber(), pageable.getSort());
        Page<StudentVaccination> pendingVaccinationsPage = vaccinationRepository.findByStatus(StudentVaccinationStatus.PENDING, pageable);
        log.info("Tìm thấy {} bản ghi tiêm chủng đang chờ duyệt trên trang {}.",
                pendingVaccinationsPage.getNumberOfElements(), pageable.getPageNumber());
        return pendingVaccinationsPage.map(vaccinationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StudentVaccinationResponseDto> getAllVaccinationsByStatus(StudentVaccinationStatus status, Pageable pageable) {
        log.info("Lấy danh sách tiêm chủng theo trạng thái: {} cho trang: {}. Kích thước trang: {}", status, pageable.getPageNumber(), pageable.getPageSize());

        if (status == null) {
            throw new IllegalArgumentException("Trạng thái (status) không được để trống khi tìm kiếm.");
        }

        Page<StudentVaccination> vaccinationsEntityPage = vaccinationRepository.findByStatus(status, pageable);
        log.info("Tìm thấy {} bản ghi tiêm chủng với trạng thái {} trên trang {}.",
                vaccinationsEntityPage.getNumberOfElements(), status, pageable.getPageNumber());

        return vaccinationsEntityPage.map(vaccinationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<StudentVaccinationResponseDto> getVaccinationsByStudentIdAndStatus(
            Long studentId, StudentVaccinationStatus status, Pageable pageable) {

        log.info("Lấy danh sách tiêm chủng cho học sinh ID: {} theo trạng thái: {}. Trang: {}, Kích thước: {}",
                studentId, status, pageable.getPageNumber(), pageable.getPageSize());

        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem thông tin tiêm chủng theo trạng thái");
        }

        if (status == null) {
            throw new IllegalArgumentException("Trạng thái (status) không được để trống khi tìm kiếm.");
        }

        Page<StudentVaccination> vaccinationsEntityPage =
                vaccinationRepository.findByStudent_IdAndStatus(studentId, status, pageable);

        log.info("Tìm thấy {} bản ghi tiêm chủng cho học sinh ID {} với trạng thái {} trên trang {}.",
                vaccinationsEntityPage.getNumberOfElements(), studentId, status, pageable.getPageNumber());

        return vaccinationsEntityPage.map(vaccinationMapper::toDto);
    }

    // ---- Các phương thức mới/cập nhật cho API /vaccinations/{vaccinationId} ----

    @Transactional(readOnly = true)
    public StudentVaccinationResponseDto getVaccinationResponseByIdForCurrentUser(Long vaccinationId) {
        log.info("Đang lấy thông tin bản ghi tiêm chủng ID: {} cho người dùng hiện tại.", vaccinationId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentVaccination entity = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tiêm chủng với ID: " + vaccinationId));

        Student studentOfRecord = entity.getStudent();
        if (studentOfRecord == null) {
            throw new IllegalStateException("Bản ghi tiêm chủng không liên kết với học sinh.");
        }

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, studentOfRecord, "xem thông tin tiêm chủng");
        }
        return vaccinationMapper.toDto(entity);
    }

    @Transactional
    public StudentVaccinationResponseDto updateVaccinationForCurrentUser(Long vaccinationId, StudentVaccinationRequestDto dto) {
        log.info("Bắt đầu cập nhật bản ghi tiêm chủng ID: {} bởi người dùng hiện tại.", vaccinationId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentVaccination existingVaccination = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bản ghi tiêm chủng không tồn tại với ID: " + vaccinationId));

        Student studentOfRecord = existingVaccination.getStudent();
        if (studentOfRecord == null) {
            throw new IllegalStateException("Bản ghi tiêm chủng không liên kết với học sinh.");
        }
        Long studentId = studentOfRecord.getId(); // Lấy studentId từ bản ghi

        // Kiểm tra quyền cập nhật dựa trên vai trò
        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, studentOfRecord, "cập nhật thông tin tiêm chủng");
            // Phụ huynh chỉ được cập nhật khi trạng thái là PENDING
            if (existingVaccination.getStatus() != StudentVaccinationStatus.PENDING) {
                throw new AccessDeniedException("Bạn chỉ có thể cập nhật thông tin tiêm chủng khi đang ở trạng thái 'Chờ xử lý'.");
            }
        } else {
            // Nhân viên không được cập nhật hồ sơ PENDING, họ phải dùng chức năng duyệt
            if (existingVaccination.getStatus() == StudentVaccinationStatus.PENDING) {
                throw new AccessDeniedException("Nhân viên không thể cập nhật hồ sơ đang ở trạng thái 'Chờ xử lý'. Vui lòng sử dụng chức năng duyệt.");
            }
        }

        // Cập nhật thông tin từ DTO vào entity
        vaccinationMapper.updateEntityFromRequestDto(dto, existingVaccination);
        existingVaccination.setUpdatedByUser(currentUser);

        // Xử lý file bằng chứng nếu có file mới
        MultipartFile newProofFile = dto.proofFile();
        if (newProofFile != null && !newProofFile.isEmpty()) {
            // Xóa file cũ nếu tồn tại
            if (existingVaccination.getProofPublicId() != null && !existingVaccination.getProofPublicId().isEmpty()) {
                try {
                    fileStorageService.deleteFile(existingVaccination.getProofPublicId(), existingVaccination.getProofResourceType());
                    vaccinationMapper.clearProofFileDetails(existingVaccination);
                } catch (Exception e) {
                    log.error("Lỗi xóa file cũ trên Cloudinary: {}", e.getMessage());
                }
            }
            // Tải file mới lên
            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(newProofFile, "vaccinations/student_" + studentId, "student_" + studentId + "_vacc_proof_updated");
            vaccinationMapper.updateProofFileDetailsFromUploadResult(uploadResult, existingVaccination);
        }

        // Thiết lập trạng thái và thông tin duyệt dựa trên vai trò người dùng
        if (currentUser.getRole() == UserRole.Parent) {
            // Nếu là phụ huynh, trạng thái luôn là PENDING, reset thông tin duyệt
            existingVaccination.setStatus(StudentVaccinationStatus.PENDING);
            existingVaccination.setApprovedByUser(null);
            existingVaccination.setApprovedAt(null);
            existingVaccination.setApproverNotes(null);
            log.info("Bản ghi ID {} được cập nhật bởi phụ huynh, giữ/chuyển về trạng thái PENDING.", vaccinationId);
        } else {
            // Nếu là nhân viên/admin, trạng thái luôn là APPROVE, cập nhật thông tin duyệt
            existingVaccination.setStatus(StudentVaccinationStatus.APPROVE);
            existingVaccination.setApprovedByUser(currentUser);
            existingVaccination.setApprovedAt(LocalDateTime.now());
            existingVaccination.setApproverNotes("Cập nhật và duyệt tự động bởi nhân viên.");
            log.info("Bản ghi ID {} được cập nhật bởi nhân viên, tự động chuyển sang trạng thái APPROVE.", vaccinationId);
        }

        StudentVaccination updatedEntity = vaccinationRepository.save(existingVaccination);
        log.info("Đã cập nhật bản ghi tiêm chủng ID: {}, trạng thái mới: {}", updatedEntity.getStudentVaccinationId(), updatedEntity.getStatus());

        // Gửi thông báo cho nhân viên y tế nếu phụ huynh cập nhật
        if (currentUser.getRole() == UserRole.Parent) {
            String content = String.format("Phụ huynh vừa cập nhật hồ sơ tiêm chủng cho học sinh '%s'. Hồ sơ cần được duyệt lại.", studentOfRecord.getFullName());
            String link = "/admin/vaccinations/pending"; // Link tới trang duyệt của admin/staff
            notificationService.createAndSendNotificationToRole(UserRole.MedicalStaff, content, link, currentUser.getEmail());
            log.info("Đã gửi thông báo cho MedicalStaff về việc cập nhật hồ sơ tiêm chủng của học sinh ID: {}", studentId);
        }

        return vaccinationMapper.toDto(updatedEntity);
    }

    @Transactional
    public StudentVaccinationResponseDto mediateVaccinationStatusForCurrentUser(Long vaccinationId, VaccinationStatusUpdateRequestDto statusUpdateRequestDto) {
        log.info("Bắt đầu duyệt bản ghi tiêm chủng ID: {} bởi người dùng hiện tại.", vaccinationId);
        User currentUser = authorizationService.getCurrentUserAndValidate();

        StudentVaccination vaccination = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bản ghi tiêm chủng không tồn tại với ID: " + vaccinationId));

        StudentVaccinationStatus currentStatus = vaccination.getStatus();
        StudentVaccinationStatus requestedNewStatus = statusUpdateRequestDto.newStatus();
        log.info("Bản ghi ID: {}. Trạng thái hiện tại: {}. Trạng thái mới yêu cầu: {}", vaccinationId, currentStatus, requestedNewStatus);

        if (currentStatus != StudentVaccinationStatus.PENDING) {
            throw new IllegalArgumentException("Chỉ có thể duyệt các bản ghi đang ở trạng thái 'Chờ xử lý'.");
        }
        if (requestedNewStatus != StudentVaccinationStatus.APPROVE && requestedNewStatus != StudentVaccinationStatus.   REJECTED) {
            throw new IllegalArgumentException("Khi duyệt, trạng thái mới chỉ có thể là 'Chấp nhận' hoặc 'Từ chối'.");
        }

        vaccination.setStatus(requestedNewStatus);
        vaccination.setApproverNotes(statusUpdateRequestDto.approverNotes());
        vaccination.setApprovedByUser(currentUser);
        vaccination.setApprovedAt(LocalDateTime.now());
        vaccination.setUpdatedByUser(currentUser); // Ghi nhận người duyệt cũng là người cập nhật cuối cùng

        StudentVaccination updatedVaccination = vaccinationRepository.save(vaccination);
        log.info("Đã cập nhật trạng thái cho bản ghi tiêm chủng ID {} thành {}. Duyệt bởi: {}. Ghi chú: {}",
                vaccinationId, updatedVaccination.getStatus(), currentUser.getEmail(), updatedVaccination.getApproverNotes());

        // Gửi thông báo đến phụ huynh
        sendNotificationToParent(updatedVaccination);

        return vaccinationMapper.toDto(updatedVaccination);
    }

    private void sendNotificationToParent(StudentVaccination vaccination) {
        try {
            Student student = vaccination.getStudent();
            if (student == null || student.getParentLinks() == null || student.getParentLinks().isEmpty()) {
                log.warn("Không tìm thấy thông tin phụ huynh cho học sinh ID: {} để gửi thông báo.", student != null ? student.getId() : "null");
                return;
            }

            String statusMessage = switch (vaccination.getStatus()) {
                case APPROVE -> "đã được duyệt";
                case REJECTED -> "đã bị từ chối";
                default -> null;
            };

            if (statusMessage != null) {
                String notificationContent = String.format("Hồ sơ tiêm chủng '%s' cho học sinh '%s' %s.",
                        vaccination.getVaccineName(),
                        student.getFullName(),
                        statusMessage);

                // Tạo link để điều hướng khi người dùng nhấp vào thông báo
                String linkToResource = "/vaccinations/" + vaccination.getStudentVaccinationId();
                // Lấy thông tin người gửi từ người duyệt, hoặc dùng "system" nếu không có
                String sender = vaccination.getApprovedByUser() != null ? vaccination.getApprovedByUser().getEmail() : "system";

                student.getParentLinks().forEach(link -> {
                    User parent = link.getParent();
                    if (parent != null && parent.getEmail() != null) {
                        String parentUsername = parent.getEmail();

                        // Gọi service mới để tạo, lưu và gửi thông báo
                        notificationService.createAndSendNotification(parentUsername, notificationContent, linkToResource, sender);

                        log.info("Đã yêu cầu gửi thông báo (và lưu vào DB) về hồ sơ tiêm chủng ID {} tới phụ huynh: {}", vaccination.getStudentVaccinationId(), parentUsername);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Lỗi khi chuẩn bị và yêu cầu gửi thông báo WebSocket đến phụ huynh: {}", e.getMessage(), e);
        }
    }
    @Transactional
    public void deleteVaccinationForCurrentUser(Long vaccinationId) {
        log.info("Bắt đầu yêu cầu xóa bản ghi tiêm chủng ID: {} bởi người dùng hiện tại.", vaccinationId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentVaccination vaccination = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Bản ghi tiêm chủng không tồn tại với ID: " + vaccinationId));

        Student studentOfRecord = vaccination.getStudent();
        if (studentOfRecord == null) {
            throw new IllegalStateException("Bản ghi tiêm chủng không liên kết với học sinh.");
        }

        String denialReason = "Bạn không có quyền xóa bản ghi này hoặc bản ghi không ở trạng thái cho phép xóa.";
        UserRole currentUserRole = currentUser.getRole();

        if (currentUserRole == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, studentOfRecord, "xóa thông tin tiêm chủng");
            if (vaccination.getStatus() == StudentVaccinationStatus.PENDING) {
            } else {
                denialReason = "Phụ huynh chỉ có thể xóa thông tin tiêm chủng đang ở trạng thái 'Chờ xử lý'.";
                throw new AccessDeniedException(denialReason);
            }
        }

        if (vaccination.getProofPublicId() != null && !vaccination.getProofPublicId().isEmpty()) {
            try{
                fileStorageService.deleteFile(vaccination.getProofPublicId(), vaccination.getProofResourceType());
            } catch (Exception e) {log.error("Lỗi xóa file Cloudinary: {}", e.getMessage());}
        }

        vaccinationRepository.delete(vaccination);
        log.info("Đã xóa thành công bản ghi tiêm chủng ID: {} bởi người dùng {}", vaccinationId, currentUser.getEmail());
    }

    @Transactional(readOnly = true) // Chỉ đọc dữ liệu, không thay đổi
    public String getSignedUrlForProofFile(Long vaccinationId) {
        log.info("Yêu cầu tạo signed URL cho file bằng chứng của bản ghi tiêm chủng ID: {}", vaccinationId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        StudentVaccination entity = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ghi tiêm chủng với ID: " + vaccinationId));

        Student studentOfRecord = entity.getStudent();
        if (studentOfRecord == null) {
            throw new IllegalStateException("Bản ghi tiêm chủng ID " + vaccinationId + " không liên kết với học sinh.");
        }

        // Kiểm tra quyền của người dùng hiện tại đối với việc xem file của học sinh này
        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, studentOfRecord, "truy cập file bằng chứng");
        }

        if (entity.getProofPublicId() == null || entity.getProofPublicId().isEmpty()) {
            log.warn("Bản ghi tiêm chủng ID: {} không có thông tin file bằng chứng (publicId rỗng).", vaccinationId);
            return null;
        }

        int urlDurationSeconds = 300;
        String signedUrl = fileStorageService.generateSignedUrl(
                entity.getProofPublicId(),
                entity.getProofResourceType(),
                urlDurationSeconds
        );

        if (signedUrl == null) {
            log.error("Không thể tạo signed URL cho public_id: {} của bản ghi ID: {}", entity.getProofPublicId(), vaccinationId);
            throw new FileStorageException("Lỗi khi tạo URL truy cập file. Vui lòng thử lại sau.");
        }

        log.info("Đã tạo signed URL thành công cho public_id: {} của bản ghi ID: {}", entity.getProofPublicId(), vaccinationId);
        return signedUrl;
    }
}
