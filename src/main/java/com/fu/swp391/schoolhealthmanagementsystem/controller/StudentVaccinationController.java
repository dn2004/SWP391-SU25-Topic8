package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.student.StudentVaccinationResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.StudentVaccination;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentVaccinationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students/{studentId}/vaccinations")
@RequiredArgsConstructor()
public class StudentVaccinationController {

    private final StudentVaccinationService vaccinationService;

    @Operation(summary = "Thêm mới thông tin tiêm chủng cho học sinh",
            description = "Thêm một bản ghi tiêm chủng và file bằng chứng (nếu có) cho học sinh được chỉ định bởi studentId.")
    @ApiResponse(responseCode = "201", description = "Tạo thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StudentVaccinationResponseDto.class))) // Thay đổi ở đây
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    @ApiResponse(responseCode = "404", description = "Học sinh không tìm thấy")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<StudentVaccinationResponseDto> addVaccination( // Thay đổi kiểu trả về
         @Parameter(description = "ID của học sinh")
         @PathVariable Long studentId,
         @Valid @ModelAttribute StudentVaccinationRequestDto vaccinationDto) {
        StudentVaccinationResponseDto createdVaccinationDto = vaccinationService.addVaccination(studentId, vaccinationDto);
        return new ResponseEntity<>(createdVaccinationDto, HttpStatus.CREATED);
    }

    @Operation(summary = "Lấy thông tin tiêm chủng theo ID bản ghi tiêm chủng")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = StudentVaccination.class)))
    @ApiResponse(responseCode = "404", description = "Thông tin tiêm chủng không tìm thấy")
    @GetMapping("/{vaccinationId}")
    public ResponseEntity<StudentVaccinationResponseDto> getVaccinationById(
            @Parameter(description = "ID của học sinh (để kiểm tra ngữ cảnh, có thể bỏ nếu không cần)") @PathVariable Long studentId,
            @Parameter(description = "ID của bản ghi tiêm chủng") @PathVariable Long vaccinationId) {
        // Bạn có thể thêm logic kiểm tra studentId có khớp với vaccinationId không nếu cần
        StudentVaccinationResponseDto vaccination = vaccinationService.getVaccinationResponseById(vaccinationId);
        // Kiểm tra xem vaccination này có thuộc studentId không
        if (!vaccination.studentId().equals(studentId)) { // Giả sử Student có getStudentId()
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Hoặc NOT_FOUND tùy logic
        }
        return ResponseEntity.ok(vaccination);
    }

    @Operation(summary = "Xóa thông tin tiêm chủng")
    @ApiResponse(responseCode = "204", description = "Xóa thành công")
    @ApiResponse(responseCode = "404", description = "Thông tin tiêm chủng không tìm thấy")
    @DeleteMapping("/{vaccinationId}")
    public ResponseEntity<Void> deleteVaccination(
            @Parameter(description = "ID của học sinh (để kiểm tra ngữ cảnh)") @PathVariable Long studentId,
            @Parameter(description = "ID của bản ghi tiêm chủng cần xóa") @PathVariable Long vaccinationId) {
        // Kiểm tra xem vaccination này có thuộc studentId không trước khi xóa
        StudentVaccinationResponseDto vaccination = vaccinationService.getVaccinationResponseById(vaccinationId);
        if (!vaccination.studentId().equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        vaccinationService.deleteVaccination(vaccinationId);
        return ResponseEntity.noContent().build();
    }


    /*
    // Endpoint để tải file trực tiếp từ server (proxying Cloudinary) thường không cần thiết
    // vì client có thể sử dụng trực tiếp Cloudinary URL (vaccination.getProofFileUrl()).
    // Nếu bạn VẪN MUỐN làm điều này (ví dụ để thêm lớp xác thực hoặc che giấu URL Cloudinary),
    // bạn sẽ cần lấy URL từ vaccination object, sau đó dùng một HTTP client (như RestTemplate hoặc WebClient)
    // để tải nội dung từ Cloudinary URL đó và stream nó về cho client.
    // Việc này phức tạp hơn và thường không phải là cách tiếp cận đầu tiên.

    @Operation(summary = "Tải file bằng chứng tiêm chủng (Proxy từ Cloudinary - Nâng cao)")
    @GetMapping("/proofs/download/{vaccinationId}")
    public ResponseEntity<Resource> downloadProofFileProxied(
            @Parameter(description = "ID của học sinh") @PathVariable Long studentId,
            @Parameter(description = "ID của bản ghi tiêm chủng") @PathVariable Long vaccinationId) {

        StudentVaccination vaccination = vaccinationService.getVaccinationById(vaccinationId);
        if (!vaccination.getStudent().getStudentId().equals(studentId)) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (vaccination.getProofFileUrl() == null || vaccination.getProofFileUrl().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // --- CÁCH LÀM NÂNG CAO: Proxy download ---
        // 1. Dùng HTTP Client (RestTemplate, WebClient) để GET request đến vaccination.getProofFileUrl()
        // 2. Nhận response body (byte[]) từ Cloudinary.
        // 3. Tạo một ByteArrayResource từ byte[].
        // 4. Trả về ResponseEntity<Resource> với header Content-Disposition và Content-Type phù hợp.

        // Ví dụ (đơn giản hóa, cần xử lý lỗi và cấu hình RestTemplate/WebClient):
        // RestTemplate restTemplate = new RestTemplate();
        // byte[] fileBytes = restTemplate.getForObject(vaccination.getProofFileUrl(), byte[].class);
        // if (fileBytes == null) {
        //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        // }
        // ByteArrayResource resource = new ByteArrayResource(fileBytes);
        //
        // return ResponseEntity.ok()
        //         .contentType(MediaType.parseMediaType(vaccination.getProofFileType()))
        //         .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + vaccination.getProofFileOriginalName() + "\"")
        //         .body(resource);

        // Cách đơn giản hơn: Trả về Redirect tới Cloudinary URL
        // return ResponseEntity.status(HttpStatus.FOUND)
        //       .location(URI.create(vaccination.getProofFileUrl()))
        //       .build();

        // CHO BÂY GIỜ: Client nên dùng trực tiếp URL. Endpoint này không cần thiết.
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
    */
}