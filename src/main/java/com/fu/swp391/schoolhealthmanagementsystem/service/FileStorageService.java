package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.UploadSignatureResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    CloudinaryUploadResponse uploadFile(MultipartFile file, String subFolder, String publicIdPrefix);
    void deleteFile(String publicId, String resourceType);
    String generateSignedUrl(String publicId, String resourceType, int durationInSeconds);

    UploadSignatureResponse getUploadSignature(String folder);

    String uploadEditorImage(MultipartFile file);

    void deleteEditorImage(String publicId);
}