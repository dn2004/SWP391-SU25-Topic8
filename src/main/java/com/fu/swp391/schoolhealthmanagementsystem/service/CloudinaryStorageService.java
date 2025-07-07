package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.UploadSignatureResponse;
import com.fu.swp391.schoolhealthmanagementsystem.exception.FileStorageException;
import com.fu.swp391.schoolhealthmanagementsystem.prop.CloudinaryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CloudinaryStorageService implements FileStorageService {

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public CloudinaryUploadResponse uploadFile(MultipartFile file, String subFolder, String publicIdPrefix) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Không thể tải lên file rỗng hoặc file không tồn tại.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String baseName = originalFilename;
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < originalFilename.length() - 1) {
            baseName = originalFilename.substring(0, dotIndex);
            extension = originalFilename.substring(dotIndex + 1);
        }

        // For public_id, use a UUID-based name. Cloudinary handles extensions via 'format'.
        String generatedPublicIdBase = (publicIdPrefix != null && !publicIdPrefix.isEmpty() ? publicIdPrefix + "_" : "")
                + UUID.randomUUID().toString();

        String folderPath = cloudinaryProperties.baseFolder() +
                (subFolder != null && !subFolder.isEmpty() ? "/" + subFolder : "");

        String resourceType = "raw"; // Default
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.startsWith("image/")) {
                resourceType = "image";
            } else if (contentType.startsWith("video/")) {
                resourceType = "video";
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("folder", folderPath);
        params.put("resource_type", resourceType);
        params.put("type", "private");
        params.put("overwrite", true);
        params.put("original_filename", originalFilename); // Good for metadata

        // How public_id and format are handled depends on resource_type
        if ("raw".equals(resourceType)) {
            // For raw files, it's common to include the original filename (or a version of it)
            // in the public_id, or let Cloudinary use original_filename if use_filename=true.
            // Here, we'll use the generated base and explicitly set the format if an extension exists.
            params.put("public_id", generatedPublicIdBase);
            if (!extension.isEmpty()) {
                params.put("format", extension); // For raw, this can help Cloudinary store it correctly
            }
        } else { // image or video
            params.put("public_id", generatedPublicIdBase); // Extension is handled by Cloudinary's format processing
            // No need to set params.put("format", extension) here for image/video,
            // Cloudinary derives it or it can be set for transformation.
        }


        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            log.info("File '{}' uploaded to Cloudinary as 'private'. Upload result: {}", originalFilename, uploadResult);

            String returnedPublicId = uploadResult.get("public_id").toString(); // This is what Cloudinary uses internally
            String returnedResourceType = uploadResult.get("resource_type").toString();
            String returnedFormat = uploadResult.get("format") != null ? uploadResult.get("format").toString() : "";

            // For raw files, if format is not explicitly returned but was part of the original filename, use original extension.
            if ("raw".equals(returnedResourceType) && returnedFormat.isEmpty() && !extension.isEmpty()) {
                returnedFormat = extension;
            }


            return new CloudinaryUploadResponse(
                    null,
                    returnedPublicId, // Use the public_id returned by Cloudinary
                    returnedResourceType,
                    originalFilename,
                    returnedFormat, // The format Cloudinary stored it as
                    file.getContentType()
            );

        } catch (IOException e) {
            throw new FileStorageException("Lỗi khi tải file '" + originalFilename + "' lên Cloudinary.", e);
        } catch (Exception e) {
            throw new FileStorageException("Lỗi không xác định khi tải file '" + originalFilename + "' lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId, String resourceType) {
        try {
            if (publicId == null || publicId.isEmpty()) return;
            if (resourceType == null || resourceType.isEmpty()) resourceType = "raw";

            Map params = ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "type", "private" // Specify type to ensure correct deletion context
            );
            cloudinary.uploader().destroy(publicId, params);
            log.info("Đã xóa file với public_id '{}' và resource_type '{}' (type: private) từ Cloudinary.", publicId, resourceType);
        } catch (IOException e) {
            throw new FileStorageException("Lỗi IO khi xóa file '" + publicId + "' từ Cloudinary.", e);
        } catch (Exception e) {
            throw new FileStorageException("Lỗi không xác định khi xóa file '" + publicId + "' từ Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateSignedUrl(String publicId, String resourceType, int durationInSeconds) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Không thể tạo signed URL: publicId rỗng.");
            return null;
        }
        if (resourceType == null || resourceType.isEmpty()) {
            // Default to 'raw' if not specified, but it's better if the caller knows the type.
            resourceType = "raw";
            log.warn("Resource type không được cung cấp để tạo signed URL cho publicId: {}. Mặc định là '{}'.", publicId, resourceType);
        }

        try {
            long expiresAtTimestamp = (System.currentTimeMillis() / 1000L) + durationInSeconds;

            Map<String, Object> options = new HashMap<>();
            options.put("secure", true); // Luôn sử dụng HTTPS
            options.put("expires_at", expiresAtTimestamp);
            options.put("resource_type", resourceType); // QUAN TRỌNG: resource_type phải có trong options

            // The 'format' parameter for privateDownloadUrl:
            // If null or empty, Cloudinary attempts to serve the original format.
            // This is generally what we want for a direct download link.
            String formatForApiCall = null;

            // Use cloudinary.api() to get the Api object
            String signedUrl =  cloudinary.privateDownload(
                    publicId,        // The public_id of the asset
                    formatForApiCall, // Pass null to get the original format
                    options          // Options map containing resource_type, expires_at, etc.
            );

            log.debug("Đã tạo signed URL cho publicId '{}', resourceType '{}': {}", publicId, resourceType, signedUrl);
            return signedUrl;

        } catch (Exception e) {
            log.error("Lỗi khi tạo signed URL cho public_id '{}', resourceType '{}': {}", publicId, resourceType, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public UploadSignatureResponse getUploadSignature(String folder) {
        long timestamp = System.currentTimeMillis() / 1000L;

        String folderPath = cloudinaryProperties.baseFolder() +
                (folder != null && !folder.isEmpty() ? "/" + folder : "");

        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("timestamp", timestamp);
        paramsToSign.put("folder", folderPath);

        try {
            String signature = cloudinary.apiSignRequest(paramsToSign, cloudinaryProperties.apiSecret());

            return new UploadSignatureResponse(
                    timestamp,
                    signature,
                    cloudinaryProperties.apiKey(),
                    cloudinaryProperties.cloudName(),
                    folderPath
            );
        } catch (Exception e) {
            log.error("Lỗi khi tạo chữ ký tải lên Cloudinary.", e);
            throw new FileStorageException("Không thể tạo chữ ký để tải lên: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadEditorImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Không thể tải lên file rỗng hoặc file không tồn tại.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String folderPath = cloudinaryProperties.baseFolder() + "/editor-images";

        Map<String, Object> params = new HashMap<>();
        params.put("folder", folderPath);
        params.put("resource_type", "image"); // Chỉ cho phép ảnh
        params.put("type", "upload"); // `upload` là mặc định cho public

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            log.info("Ảnh từ editor '{}' đã được tải lên Cloudinary. Upload result: {}", originalFilename, uploadResult);

            // Trả về URL an toàn của ảnh
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new FileStorageException("Lỗi khi tải ảnh editor '" + originalFilename + "' lên Cloudinary.", e);
        } catch (Exception e) {
            throw new FileStorageException("Lỗi không xác định khi tải ảnh editor '" + originalFilename + "' lên Cloudinary: " + e.getMessage(), e);
        }
    }

    public String uploadBlogThumbnail(MultipartFile file) {
        // 1. Validation: Giữ nguyên, rất tốt
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Không thể tải lên file rỗng hoặc file không tồn tại.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String folderPath = cloudinaryProperties.baseFolder() + "/blog-thumbnails";

        // 2. Public ID: Giữ nguyên, rất tốt
        String publicId = "thumbnail_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        // 3. Tối ưu Transformation: Sửa lỗi quan trọng nhất
        // Tạo đối tượng Transformation thay vì Map
        Transformation transformation = new Transformation<>()
                .width(800)
                .height(600)
                .crop("limit")       // Giới hạn kích thước mà không làm méo ảnh
                .quality("auto:good"); // Tối ưu chất lượng tự động

        try {
            // 4. Tối ưu cách tạo params và cách upload
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folderPath,
                    "resource_type", "image",
                    "public_id", publicId,
                    "overwrite", false,
                    "transformation", transformation // Truyền thẳng đối tượng Transformation
            );

            // Sử dụng file.getInputStream() để hiệu quả hơn về bộ nhớ
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // 5. Logging: Giữ nguyên, rất tốt
            log.info("Thumbnail '{}' đã được tải lên Cloudinary. Public ID: {}. URL: {}",
                    originalFilename,
                    uploadResult.get("public_id"),
                    uploadResult.get("secure_url"));

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            // Bắt lỗi IO cụ thể khi đọc file
            log.error("Lỗi IO khi đọc file '{}'.", originalFilename, e);
            throw new FileStorageException("Lỗi khi đọc file để tải lên Cloudinary: " + originalFilename, e);
        } catch (Exception e) {
            // 6. Cải thiện xử lý lỗi chung
            // Log đầy đủ stack trace để dễ debug
            log.error("Lỗi không xác định khi tải thumbnail '{}' lên Cloudinary.", originalFilename, e);
            throw new FileStorageException("Lỗi không xác định khi tải thumbnail lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEditorImage(String publicId) {
        try {
            if (publicId == null || publicId.isEmpty()) return;

            // Đối với ảnh từ editor, chúng ta mặc định resource_type là 'image' và type là 'upload' (public)
            Map params = ObjectUtils.asMap(
                    "resource_type", "image"
            );
            cloudinary.uploader().destroy(publicId, params);
            log.info("Đã xóa ảnh công khai (editor) với public_id '{}' từ Cloudinary.", publicId);
        } catch (IOException e) {
            throw new FileStorageException("Lỗi IO khi xóa ảnh editor '" + publicId + "' từ Cloudinary.", e);
        } catch (Exception e) {
            throw new FileStorageException("Lỗi không xác định khi xóa ảnh editor '" + publicId + "' từ Cloudinary: " + e.getMessage(), e);
        }
    }
}