package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.UploadSignatureResponse;
import com.fu.swp391.schoolhealthmanagementsystem.service.FileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileStorageService fileStorageService;

    @GetMapping("/upload-signature")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UploadSignatureResponse> getUploadSignature(@RequestParam(required = false) String folder) {
        return ResponseEntity.ok(fileStorageService.getUploadSignature(folder));
    }
}

