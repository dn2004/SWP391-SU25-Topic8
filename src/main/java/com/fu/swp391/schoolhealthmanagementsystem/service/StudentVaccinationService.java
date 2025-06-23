package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.VaccinationStatusUpdateRequestDto;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    // Phương thức này vẫn giữ studentId vì nó tạo mới cho một student cụ thể
    @Transactional
    public StudentVaccinationResponseDto addVaccination(Long studentId, StudentVaccinationRequestDto dto) {
        log.info("Bắt đầu thêm thông tin tiêm chủng cho học sinh ID: {}", studentId);
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        StudentVaccinationStatus vaccinationStatus = StudentVaccinationStatus.PENDING;

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "thêm thông tin tiêm chủng");
        } else {
            vaccinationStatus = StudentVaccinationStatus.APPROVE;
        }

        StudentVaccination vaccinationEntity = vaccinationMapper.requestDtoToEntity(dto);
        vaccinationEntity.setStudent(student);
        vaccinationEntity.setStatus(vaccinationStatus);
        log.info("Gán trạng thái mặc định PENDING cho bản ghi tiêm chủng mới của học sinh ID: {}", studentId);

        MultipartFile proofFile = dto.proofFile();
        if (proofFile != null && !proofFile.isEmpty()) {
            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(proofFile, "vaccinations/student_" + studentId, "student_" + studentId + "_vacc_proof");
            vaccinationMapper.updateProofFileDetailsFromUploadResult(uploadResult, vaccinationEntity);
        }

        StudentVaccination savedEntity = vaccinationRepository.save(vaccinationEntity);
        log.info("Đã lưu bản ghi tiêm chủng với ID: {} và trạng thái: {}", savedEntity.getStudentVaccinationId(), savedEntity.getStatus());
        return vaccinationMapper.toDto(savedEntity);
    }

    @Transactional(readOnly = true)
    public Page<StudentVaccinationResponseDto> getAllVaccinationsByStudentIdPage(Long studentId, Pageable pageable) {
        log.info("Lấy danh sách tiêm chủng phân trang cho học sinh ID: {}. Trang: {}", studentId, pageable.getPageNumber());
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, student, "xem danh sách tiêm chủng");
        }

        Page<StudentVaccination> vaccinationsEntityPage = vaccinationRepository.findByStudent_Id(studentId, pageable);
        log.info("Tìm thấy {} bản ghi tiêm chủng cho học sinh ID {} trên trang {}.", vaccinationsEntityPage.getNumberOfElements(), studentId, pageable.getPageNumber());
        return vaccinationsEntityPage.map(vaccinationMapper::toDto);
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

        // Kiểm tra quyền cập nhật
        if (currentUser.getRole() == UserRole.Parent) {
            authorizationService.authorizeParentAction(currentUser, studentOfRecord, "cập nhật thông tin tiêm chủng");
            if (existingVaccination.getStatus() != StudentVaccinationStatus.PENDING) {
                throw new AccessDeniedException("Bạn chỉ có thể cập nhật thông tin tiêm chủng khi đang ở trạng thái 'Chờ xử lý'.");
            }
        }

        StudentVaccinationStatus currentStatusBeforeUpdate = existingVaccination.getStatus();
        vaccinationMapper.updateEntityFromRequestDto(dto, existingVaccination);

        MultipartFile newProofFile = dto.proofFile();
        if (newProofFile != null && !newProofFile.isEmpty()) {
            if (existingVaccination.getProofPublicId() != null && !existingVaccination.getProofPublicId().isEmpty()) {
                try {
                    fileStorageService.deleteFile(existingVaccination.getProofPublicId(), existingVaccination.getProofResourceType());
                    vaccinationMapper.clearProofFileDetails(existingVaccination);
                } catch (Exception e) { log.error("Lỗi xóa file cũ trên Cloudinary: {}", e.getMessage());}
            }
            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(newProofFile, "vaccinations/student_" + studentId, "student_" + studentId + "_vacc_proof_updated");
            vaccinationMapper.updateProofFileDetailsFromUploadResult(uploadResult, existingVaccination);
        }

        if (currentStatusBeforeUpdate != StudentVaccinationStatus.PENDING) {
            existingVaccination.setStatus(StudentVaccinationStatus.PENDING);
            existingVaccination.setApprovedByUser(null);
            existingVaccination.setApprovedAt(null);
            existingVaccination.setApproverNotes(null);
            log.info("Bản ghi ID {} có trạng thái '{}', chuyển về PENDING sau cập nhật.", vaccinationId, currentStatusBeforeUpdate);
        } else {
            log.info("Bản ghi ID {} đang PENDING, giữ nguyên trạng thái PENDING sau cập nhật.", vaccinationId);
        }

        StudentVaccination updatedEntity = vaccinationRepository.save(existingVaccination);
        log.info("Đã cập nhật bản ghi tiêm chủng ID: {}, trạng thái mới: {}", updatedEntity.getStudentVaccinationId(), updatedEntity.getStatus());
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

        StudentVaccination updatedVaccination = vaccinationRepository.save(vaccination);
        log.info("Đã cập nhật trạng thái cho bản ghi tiêm chủng ID {} thành {}. Duyệt bởi: {}. Ghi chú: {}",
                vaccinationId, updatedVaccination.getStatus(), currentUser.getEmail(), updatedVaccination.getApproverNotes());
        return vaccinationMapper.toDto(updatedVaccination);
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