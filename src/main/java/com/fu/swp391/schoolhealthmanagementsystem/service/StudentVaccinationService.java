package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Student;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.StudentVaccinationMapper; // Import mapper
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.StudentVaccinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.beans.factory.annotation.Autowired; // Không cần nếu dùng @RequiredArgsConstructor
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor // Sử dụng constructor injection
public class StudentVaccinationService {

    private final StudentVaccinationRepository vaccinationRepository;
    private final FileStorageService fileStorageService;
    private final StudentRepository studentRepository;
    private final StudentVaccinationMapper vaccinationMapper;

    @Transactional
    public StudentVaccinationResponseDto addVaccination(Long studentId, StudentVaccinationRequestDto dto) {
        log.info("Bắt đầu thêm thông tin tiêm chủng cho học sinh ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh với ID: " + studentId));

        StudentVaccination vaccinationEntity = vaccinationMapper.requestDtoToEntity(dto);
        vaccinationEntity.setStudent(student);


        MultipartFile proofFile = dto.proofFile();
        if (proofFile != null && !proofFile.isEmpty()) {
            log.info("Có file bằng chứng được cung cấp: {}", proofFile.getOriginalFilename());
            String subFolder = "vaccinations/student_" + studentId;
            String publicIdPrefix = "student_" + studentId + "_vacc_proof";

            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(proofFile, subFolder, publicIdPrefix);
            log.info("File đã được tải lên Cloudinary. URL: {}, Public ID: {}", uploadResult.url(), uploadResult.publicId());

            vaccinationMapper.updateProofFileDetailsFromUploadResult(uploadResult, vaccinationEntity);
        } else {
            log.info("Không có file bằng chứng nào được cung cấp cho học sinh ID: {}", studentId);
        }

        StudentVaccination savedEntity = vaccinationRepository.save(vaccinationEntity);
        log.info("Đã lưu bản ghi tiêm chủng với ID: {}", savedEntity.getStudentVaccinationId());
        log.info("status mặc định dự kiến là {}", savedEntity.getStatus());

        // Sử dụng mapper để chuyển đổi Entity đã lưu sang DTO
        return vaccinationMapper.toDto(savedEntity);
    }

    public StudentVaccinationResponseDto getVaccinationResponseById(Long vaccinationId) {
        log.info("Đang lấy thông tin bản ghi tiêm chủng với ID: {}", vaccinationId);
        StudentVaccination entity = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bản ghi tiêm chủng với ID: {}", vaccinationId);
                    return new ResourceNotFoundException("Không tìm thấy bản ghi tiêm chủng với ID: " + vaccinationId);
                });
        // Sử dụng mapper
        return vaccinationMapper.toDto(entity);
    }

    // Tương tự cho phương thức update, bạn cũng sẽ dùng mapper.toDto() ở cuối
    @Transactional
    public StudentVaccinationResponseDto updateVaccination(Long vaccinationId, StudentVaccinationRequestDto dto) {
        log.info("Bắt đầu cập nhật bản ghi tiêm chủng với ID: {}", vaccinationId);
        StudentVaccination existingVaccination = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> {
                    log.error("Không tìm thấy bản ghi tiêm chủng với ID: {}", vaccinationId);
                    return new ResourceNotFoundException("Bản ghi tiêm chủng không tồn tại với ID: " + vaccinationId);
                });

        // Sử dụng MapStruct để cập nhật các trường cơ bản từ DTO vào existingVaccination
        vaccinationMapper.updateEntityFromRequestDto(dto, existingVaccination);
        log.info("Đã cập nhật các trường cơ bản từ DTO cho bản ghi tiêm chủng ID: {}", vaccinationId);

        MultipartFile newProofFile = dto.proofFile();
        if (newProofFile != null && !newProofFile.isEmpty()) {
            log.info("Có file bằng chứng mới được cung cấp để cập nhật: {}", newProofFile.getOriginalFilename());

            // 1. Xóa file cũ trên Cloudinary (nếu có)
            if (existingVaccination.getProofPublicId() != null && !existingVaccination.getProofPublicId().isEmpty()) {
                log.info("Đang xóa file bằng chứng cũ trên Cloudinary với Public ID: {}", existingVaccination.getProofPublicId());
                try {
                    fileStorageService.deleteFile(existingVaccination.getProofPublicId(), existingVaccination.getProofResourceType());
                    log.info("Đã xóa file cũ thành công.");
                    // Sau khi xóa thành công, nên xóa thông tin file cũ khỏi entity để tránh trường hợp upload file mới lỗi
                    // mà entity vẫn còn thông tin file cũ không hợp lệ.
                    existingVaccination.setProofFileOriginalName(null);
                    existingVaccination.setProofFileUrl(null);
                    existingVaccination.setProofFileType(null);
                    existingVaccination.setProofPublicId(null);
                    existingVaccination.setProofResourceType(null);
                } catch (Exception e) {
                    log.error("Lỗi khi xóa file cũ trên Cloudinary với Public ID: {}. Lỗi: {}", existingVaccination.getProofPublicId(), e.getMessage());
                    // Cân nhắc: Có nên dừng lại ở đây không nếu xóa file cũ lỗi?
                    // Hoặc ít nhất là không xóa thông tin file cũ khỏi entity nếu xóa lỗi.
                }
            }

            // 2. Upload file mới
            // Cần studentId để tạo đường dẫn thư mục, lấy từ existingVaccination
            Long studentId = existingVaccination.getStudent().getStudentId();
            String subFolder = "vaccinations/student_" + studentId;
            String publicIdPrefix = "student_" + studentId + "_vacc_proof_updated"; // Thêm suffix để dễ nhận biết

            CloudinaryUploadResponse uploadResult = fileStorageService.uploadFile(newProofFile, subFolder, publicIdPrefix);
            log.info("File mới đã được tải lên Cloudinary. URL: {}, Public ID: {}", uploadResult.url(), uploadResult.publicId());

            // 3. Sử dụng MapStruct để cập nhật thông tin file mới vào existingVaccination
            vaccinationMapper.updateProofFileDetailsFromUploadResult(uploadResult, existingVaccination);
            log.info("Đã cập nhật thông tin file mới vào bản ghi tiêm chủng ID: {}", vaccinationId);

        } else {
            log.info("Không có file bằng chứng mới nào được cung cấp cho việc cập nhật bản ghi ID: {}. Giữ nguyên file cũ (nếu có).", vaccinationId);
            // Nếu DTO có một cờ yêu cầu xóa file hiện tại mà không upload file mới, bạn sẽ xử lý ở đây.
            // Ví dụ: if (dto.isRemoveProofFile() && existingVaccination.getProofPublicId() != null) { ... xóa file và clear trường ... }
        }

        StudentVaccination updatedEntity = vaccinationRepository.save(existingVaccination);
        log.info("Đã cập nhật và lưu bản ghi tiêm chủng với ID: {}", updatedEntity.getStudentVaccinationId());
        return vaccinationMapper.toDto(updatedEntity);
    }


    // Phương thức deleteVaccination không cần trả về DTO
    @Transactional
    public void deleteVaccination(Long vaccinationId) {
        // ... (logic xóa như trước) ...
        log.info("Bắt đầu xóa bản ghi tiêm chủng với ID: {}", vaccinationId);
        StudentVaccination vaccination = vaccinationRepository.findById(vaccinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination record not found with id: " + vaccinationId));


        if (vaccination.getProofPublicId() != null && !vaccination.getProofPublicId().isEmpty()) {
            log.info("Đang xóa file bằng chứng trên Cloudinary với Public ID: {}", vaccination.getProofPublicId());
            try {
                fileStorageService.deleteFile(vaccination.getProofPublicId(), vaccination.getProofResourceType());
                log.info("Đã xóa file trên Cloudinary thành công cho Public ID: {}", vaccination.getProofPublicId());
            } catch (Exception e) {
                log.error("Lỗi khi xóa file trên Cloudinary với Public ID: {}. Lỗi: {}", vaccination.getProofPublicId(), e.getMessage());
            }
        } else {
            log.info("Không có file bằng chứng nào trên Cloudinary để xóa cho bản ghi tiêm chủng ID: {}", vaccinationId);
        }
        vaccinationRepository.delete(vaccination);
        log.info("Đã xóa thành công bản ghi tiêm chủng với ID: {}", vaccinationId);
    }
}