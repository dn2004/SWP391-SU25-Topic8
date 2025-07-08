package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Blog File Management", description = "API để quản lý file cho blog (thumbnail, images)")
@SecurityRequirement(name = "bearerAuth")
public class BlogFileController {

    private final BlogService blogService;

    @Operation(
            summary = "Tải lên ảnh thumbnail cho bài viết",
            description = """
### Mô tả
Tải lên một file ảnh để sử dụng làm thumbnail cho bài viết. File sẽ được lưu trữ trên Cloudinary và trả về URL.
- **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `MedicalStaff`, hoặc `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tải lên thumbnail thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"thumbnailUrl\": \"https://res.cloudinary.com/...\"}"))),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ hoặc quá lớn", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @PostMapping(value = "/upload-thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadThumbnail(
            @Parameter(description = "File ảnh thumbnail (định dạng: jpg, jpeg, png, gif, webp)")
            @RequestParam("file") MultipartFile file) {
        log.info("API POST /api/blogs/upload-thumbnail được gọi để upload thumbnail");
        String thumbnailUrl = blogService.uploadThumbnail(file);
        return ResponseEntity.ok(Map.of("thumbnailUrl", thumbnailUrl));
    }

    @Operation(
            summary = "Xóa ảnh thumbnail của bài viết",
            description = """
### Mô tả
Xóa ảnh thumbnail khỏi Cloudinary dựa vào URL của ảnh.
- **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `MedicalStaff`, hoặc `StaffManager`.
"""
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thumbnail thành công"),
            @ApiResponse(responseCode = "400", description = "URL không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @DeleteMapping("/delete-thumbnail")
    public ResponseEntity<Map<String, String>> deleteThumbnail(
            @Parameter(description = "URL của thumbnail cần xóa trên Cloudinary")
            @RequestParam("thumbnailUrl") String thumbnailUrl) {
        log.info("API DELETE /api/blogs/delete-thumbnail được gọi để xóa thumbnail: {}", thumbnailUrl);
        blogService.deleteThumbnail(thumbnailUrl);
        return ResponseEntity.ok(Map.of("message", "Thumbnail đã được xóa thành công"));
    }
}
