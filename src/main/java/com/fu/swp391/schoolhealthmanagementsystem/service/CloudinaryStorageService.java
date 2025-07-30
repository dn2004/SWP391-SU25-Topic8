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
            log.warn("[CLOUDINARY] Không thể tải lên file rỗng hoặc file không tồn tại.");
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

        // Đối với public_id, sử dụng tên dựa trên UUID. Cloudinary sẽ xử lý phần mở rộng qua 'format'.
        String generatedPublicIdBase = (publicIdPrefix != null && !publicIdPrefix.isEmpty() ? publicIdPrefix + "_" : "")
                + UUID.randomUUID().toString();

        String folderPath = cloudinaryProperties.baseFolder() +
                (subFolder != null && !subFolder.isEmpty() ? "/" + subFolder : "");

        String resourceType = "raw"; // Mặc định
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
        params.put("original_filename", originalFilename); // Tốt cho metadata

        // Cách xử lý public_id và format phụ thuộc vào resource_type
        if ("raw".equals(resourceType)) {
            // Đối với file raw, thường nên đưa tên file gốc (hoặc một phiên bản của nó) vào public_id, hoặc để Cloudinary sử dụng original_filename nếu use_filename=true.
            // Ở đây, sử dụng tên sinh tự động và đặt format nếu có phần mở rộng.
            params.put("public_id", generatedPublicIdBase);
            if (!extension.isEmpty()) {
                params.put("format", extension); // Đối với raw, giúp Cloudinary lưu đúng định dạng
            }
        } else { // image hoặc video
            params.put("public_id", generatedPublicIdBase); // Phần mở rộng được Cloudinary xử lý qua format
            // Không cần set params.put("format", extension) cho image/video,
            // Cloudinary sẽ tự động nhận diện hoặc có thể set khi transform.
        }


        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            log.info("[CLOUDINARY] File '{}' uploaded lên Cloudinary (type: private). Kết quả: {}", originalFilename, uploadResult);

            String returnedPublicId = uploadResult.get("public_id").toString(); // Đây là public_id Cloudinary sử dụng nội bộ
            String returnedResourceType = uploadResult.get("resource_type").toString();
            String returnedFormat = uploadResult.get("format") != null ? uploadResult.get("format").toString() : "";

            // Đối với file raw, nếu format không trả về nhưng file gốc có phần mở rộng thì dùng phần mở rộng đó.
            if ("raw".equals(returnedResourceType) && returnedFormat.isEmpty() && !extension.isEmpty()) {
                returnedFormat = extension;
            }


            return new CloudinaryUploadResponse(
                    null,
                    returnedPublicId, // Sử dụng public_id trả về từ Cloudinary
                    returnedResourceType,
                    originalFilename,
                    returnedFormat, // Định dạng Cloudinary lưu trữ
                    file.getContentType()
            );

        } catch (IOException e) {
            log.error("[CLOUDINARY] Lỗi IO khi tải file '{}' lên Cloudinary.", originalFilename, e);
            throw new FileStorageException("Lỗi khi tải file '" + originalFilename + "' lên Cloudinary.", e);
        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi không xác định khi tải file '{}' lên Cloudinary: {}", originalFilename, e.getMessage(), e);
            throw new FileStorageException("Lỗi không xác định khi tải file '" + originalFilename + "' lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId, String resourceType) {
        try {
            if (publicId == null || publicId.isEmpty()) {
                log.warn("[CLOUDINARY] Không thể xóa file: publicId rỗng.");
                return;
            }
            if (resourceType == null || resourceType.isEmpty()) resourceType = "raw";

            Map params = ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "type", "private" // Chỉ định type để đảm bảo xóa đúng ngữ cảnh
            );
            cloudinary.uploader().destroy(publicId, params);
            log.info("[CLOUDINARY] Đã xóa file với public_id '{}' và resource_type '{}' (type: private) từ Cloudinary.", publicId, resourceType);
        } catch (IOException e) {
            log.error("[CLOUDINARY] Lỗi IO khi xóa file '{}' từ Cloudinary.", publicId, e);
            throw new FileStorageException("Lỗi IO khi xóa file '" + publicId + "' từ Cloudinary.", e);
        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi không xác định khi xóa file '{}' từ Cloudinary: {}", publicId, e.getMessage(), e);
            throw new FileStorageException("Lỗi không xác định khi xóa file '" + publicId + "' từ Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateSignedUrl(String publicId, String resourceType, int durationInSeconds) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("[CLOUDINARY] Không thể tạo signed URL: publicId rỗng.");
            return null;
        }
        if (resourceType == null || resourceType.isEmpty()) {
            resourceType = "raw";
            log.warn("[CLOUDINARY] Resource type không được cung cấp để tạo signed URL cho publicId: {}. Mặc định là '{}'.", publicId, resourceType);
        }
        try {
            long expiresAtTimestamp = (System.currentTimeMillis() / 1000L) + durationInSeconds;

            Map<String, Object> options = new HashMap<>();
            options.put("secure", true); // Luôn sử dụng HTTPS
            options.put("expires_at", expiresAtTimestamp);
            options.put("resource_type", resourceType); // QUAN TRỌNG: resource_type phải có trong options

            // Tham số 'format' cho privateDownloadUrl:
            // Nếu null hoặc rỗng, Cloudinary sẽ trả về định dạng gốc.
            // Đây là điều mong muốn cho link tải trực tiếp.
            String formatForApiCall = null;

            // Sử dụng cloudinary.api() để lấy đối tượng Api
            String signedUrl =  cloudinary.privateDownload(
                    publicId,        // public_id của tài nguyên
                    formatForApiCall, // Truyền null để lấy định dạng gốc
                    options          // Map options gồm resource_type, expires_at, ...
            );

            log.info("[CLOUDINARY] Đã tạo signed URL cho publicId '{}', resourceType '{}': {}", publicId, resourceType, signedUrl);
            return signedUrl;

        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi khi tạo signed URL cho public_id '{}', resourceType '{}': {}", publicId, resourceType, e.getMessage(), e);
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
            log.info("[CLOUDINARY] Đã tạo chữ ký upload cho folder '{}'.", folderPath);
            return new UploadSignatureResponse(
                    timestamp,
                    signature,
                    cloudinaryProperties.apiKey(),
                    cloudinaryProperties.cloudName(),
                    folderPath
            );
        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi khi tạo chữ ký tải lên Cloudinary cho folder '{}'.", folderPath, e);
            throw new FileStorageException("Không thể tạo chữ ký để tải lên: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadEditorImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("[CLOUDINARY] Không thể tải lên file rỗng hoặc file không tồn tại (editor image).");
            throw new FileStorageException("Không thể tải lên file rỗng hoặc file không tồn tại.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String folderPath = cloudinaryProperties.baseFolder() + "/blog_images";

        Map<String, Object> params = new HashMap<>();
        params.put("folder", folderPath);
        params.put("resource_type", "image"); // Chỉ cho phép ảnh
        params.put("type", "upload"); // `upload` là mặc định cho public

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            log.info("[CLOUDINARY] Ảnh từ editor '{}' đã được tải lên Cloudinary. Kết quả: {}", originalFilename, uploadResult);

            // Trả về URL an toàn của ảnh
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            log.error("[CLOUDINARY] Lỗi IO khi tải ảnh editor '{}' lên Cloudinary.", originalFilename, e);
            throw new FileStorageException("Lỗi khi tải ảnh editor '" + originalFilename + "' lên Cloudinary.", e);
        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi không xác định khi tải ảnh editor '{}' lên Cloudinary: {}", originalFilename, e.getMessage(), e);
            throw new FileStorageException("Lỗi không xác định khi tải ảnh editor '" + originalFilename + "' lên Cloudinary: " + e.getMessage(), e);
        }
    }

    public String uploadBlogThumbnail(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("[CLOUDINARY] Không thể tải lên file rỗng hoặc file không tồn tại (blog thumbnail).");
            throw new FileStorageException("Không thể tải lên file rỗng hoặc file không tồn tại.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String folderPath = cloudinaryProperties.baseFolder() + "/blog_thumbnails";

        // Public ID: Giữ nguyên, rất tốt
        String publicId = "thumbnail_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

        // Tối ưu hóa Transformation: Sửa lỗi quan trọng nhất
        // Tạo đối tượng Transformation thay vì Map
        Transformation transformation = new Transformation()
                    .width(800)
                    .height(600)
                    .crop("limit")
                    .quality("auto:good");

        try {
            // Tối ưu cách tạo params và upload
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folderPath,
                    "resource_type", "image",
                    "public_id", publicId,
                    "overwrite", false,
                    "transformation", transformation // Truyền thẳng đối tượng Transformation
            );

            // Sử dụng file.getInputStream() để tiết kiệm bộ nhớ
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // Logging: Giữ nguyên
            log.info("[CLOUDINARY] Thumbnail '{}' đã được tải lên Cloudinary. Public ID: {}. URL: {}", originalFilename, uploadResult.get("public_id"), uploadResult.get("secure_url"));
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            log.error("[CLOUDINARY] Lỗi IO khi đọc file '{}' để upload thumbnail.", originalFilename, e);
            throw new FileStorageException("Lỗi khi đọc file để tải lên Cloudinary: " + originalFilename, e);
        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi không xác định khi tải thumbnail '{}' lên Cloudinary: {}", originalFilename, e.getMessage(), e);
            throw new FileStorageException("Lỗi không xác định khi tải thumbnail lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEditorImage(String publicId) {
        try {
            if (publicId == null || publicId.isEmpty()) {
                log.warn("[CLOUDINARY] Không thể xóa ảnh editor: publicId rỗng.");
                return;
            }

            // Đối với ảnh từ editor, mặc định resource_type là 'image' và type là 'upload' (public)
            Map params = ObjectUtils.asMap(
                    "resource_type", "image"
            );
            cloudinary.uploader().destroy(publicId, params);
            log.info("[CLOUDINARY] Đã xóa ảnh công khai (editor) với public_id '{}' từ Cloudinary.", publicId);
        } catch (IOException e) {
            log.error("[CLOUDINARY] Lỗi IO khi xóa ảnh editor '{}' từ Cloudinary.", publicId, e);
            throw new FileStorageException("Lỗi IO khi xóa ảnh editor '" + publicId + "' từ Cloudinary.", e);
        } catch (Exception e) {
            log.error("[CLOUDINARY] Lỗi không xác định khi xóa ảnh editor '{}' từ Cloudinary: {}", publicId, e.getMessage(), e);
            throw new FileStorageException("Lỗi không xác định khi xóa ảnh editor '" + publicId + "' từ Cloudinary: " + e.getMessage(), e);
        }
    }
}
