package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.UploadSignatureResponse;
import com.fu.swp391.schoolhealthmanagementsystem.service.FileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/upload-signature")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadSignatureResponse> getUploadSignature(@RequestParam(required = false) String folder) {
        log.info("API GET /api/files/upload-signature được gọi với folder: '{}'", folder);
        return ResponseEntity.ok(fileStorageService.getUploadSignature(folder));
    }

    @PostMapping("/upload-editor-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        log.info("API POST /api/files/upload-editor-image được gọi để tải lên file: '{}'", file.getOriginalFilename());
        // Upload file lên Cloudinary và nhận lại URL
        String location = fileStorageService.uploadEditorImage(file);
        // Trả về JSON theo định dạng mà các editor phổ biến (như TinyMCE) mong muốn
        return ResponseEntity.ok(Map.of("location", location));
    }

    @PostMapping("/delete-editor-image")
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
