package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.UploadSignatureResponse;
import com.fu.swp391.schoolhealthmanagementsystem.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Management", description = "APIs quản lý file upload và download")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/upload-signature")
    @Operation(
            summary = "Lấy signature để upload file lên Cloudinary",
            description = """
                    ### Mô tả
                    Tạo một chữ ký (signature) an toàn phía server để client có thể upload file trực tiếp lên Cloudinary mà không cần thông qua server.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy signature thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UploadSignatureResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadSignatureResponse> getUploadSignature(@RequestParam(required = false) String folder) {
        log.info("API GET /api/files/upload-signature được gọi với folder: '{}'", folder);
        return ResponseEntity.ok(fileStorageService.getUploadSignature(folder));
    }

    @PostMapping("/upload-editor-image")
    @Operation(
            summary = "Upload ảnh cho trình soạn thảo văn bản",
            description = """
                    ### Mô tả
                    Upload một file ảnh để sử dụng trong các trình soạn thảo văn bản (ví dụ: TinyMCE).
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upload ảnh thành công",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        log.info("API POST /api/files/upload-editor-image được gọi để tải lên file: '{}'", file.getOriginalFilename());
        // Upload file lên Cloudinary và nhận lại URL
        String location = fileStorageService.uploadEditorImage(file);
        // Trả về JSON theo định dạng mà các editor phổ biến (như TinyMCE) mong muốn
        return ResponseEntity.ok(Map.of("location", location));
    }

    @PostMapping("/delete-editor-image")
    @Operation(
            summary = "Xóa ảnh đã upload khỏi Cloudinary",
            description = """
                    ### Mô tả
                    Xóa một ảnh đã upload khỏi Cloudinary bằng `publicId` của nó.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa ảnh thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "PublicId không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEditorImage(@RequestBody Map<String, String> payload) {
        String publicId = payload.get("publicId");
        log.info("API POST /api/files/delete-editor-image được gọi để xóa ảnh với publicId: '{}'", publicId);
        if (publicId != null && !publicId.isEmpty()) {
            fileStorageService.deleteEditorImage(publicId);
        }
        return ResponseEntity.ok().build();
    }
}
