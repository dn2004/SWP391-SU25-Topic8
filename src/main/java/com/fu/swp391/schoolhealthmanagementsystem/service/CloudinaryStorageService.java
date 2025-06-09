package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fu.swp391.schoolhealthmanagementsystem.dto.cloudinary.CloudinaryUploadResponse;
import com.fu.swp391.schoolhealthmanagementsystem.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class CloudinaryStorageService implements FileStorageService {
    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloudinary.base_folder}")
    String baseFolder;


    @Override
    public CloudinaryUploadResponse uploadFile(MultipartFile file, String subFolder, String publicIdPrefix) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Không thể tải lên file rỗng hoặc file không tồn tại.");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            String uniqueFileNamePart = (publicIdPrefix != null && !publicIdPrefix.isEmpty() ? publicIdPrefix + "_" : "") + UUID.randomUUID();
            // Cloudinary sẽ tự thêm extension nếu không có trong public_id và resource_type là image/video
            // Để chắc chắn, bạn có thể giữ lại extension nếu muốn public_id có extension
            // String extension = "";
            // int i = originalFilename.lastIndexOf('.');
            // if (i > 0) {
            //     extension = originalFilename.substring(i);
            // }
            // String uniqueFileName = uniqueFileNamePart + extension;

            String folderPath = baseFolder + (subFolder != null && !subFolder.isEmpty() ? "/" + subFolder : "");

            String resourceType = "raw";
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    resourceType = "image";
                } else if (contentType.startsWith("video/")) {
                    resourceType = "video";
                }
            }

            Map params = ObjectUtils.asMap(
                    "public_id", uniqueFileNamePart, // Sử dụng phần không có extension ở đây
                    "folder", folderPath,
                    "resource_type", resourceType,
                    "overwrite", true,
                    "original_filename", originalFilename // Lưu tên file gốc để tham khảo
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            return new CloudinaryUploadResponse(
                    uploadResult.get("secure_url").toString(),
                    uploadResult.get("public_id").toString(),
                    uploadResult.get("resource_type").toString(),
                    originalFilename,
                    uploadResult.get("format") != null ? uploadResult.get("format").toString() : "",
                    file.getContentType() // Gán contentType ở đây
            );

        } catch (IOException e) {
            throw new FileStorageException("Lỗi khi tải file '" + originalFilename + "' lên Cloudinary.", e);
        } catch (Exception e) { // Bắt các lỗi chung khác từ Cloudinary
            throw new FileStorageException("Lỗi không xác định khi tải file '" + originalFilename + "' lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String publicId, String resourceType) {
        try {
            if (publicId == null || publicId.isEmpty()) {
                return;
            }
            // Resource type rất quan trọng để xóa đúng đối tượng trên Cloudinary
            // Nếu không cung cấp, Cloudinary có thể không tìm thấy hoặc xóa sai
            if (resourceType == null || resourceType.isEmpty()) {
                // Bạn có thể thử đoán resource type dựa trên public_id hoặc mặc định là "image"
                // Tuy nhiên, tốt nhất là lưu và truyền đúng resource_type
                resourceType = "raw"; // Hoặc một default an toàn hơn nếu bạn có quy ước
                // Logger.warn("Resource type không được cung cấp khi xóa file. Mặc định là 'raw'. Public ID: " + publicId);
            }
            Map params = ObjectUtils.asMap(
                    "resource_type", resourceType
            );
            cloudinary.uploader().destroy(publicId, params);
        } catch (IOException e) {
            throw new FileStorageException("Lỗi khi xóa file với public_id '" + publicId + "' từ Cloudinary.", e);
        } catch (Exception e) {
            throw new FileStorageException("Lỗi không xác định khi xóa file '" + publicId + "' từ Cloudinary: " + e.getMessage(), e);
        }
    }
}