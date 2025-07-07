package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class BlogFileController {

    private final BlogService blogService;

    @Operation(summary = "Upload thumbnail cho blog",
            description = "Upload ảnh thumbnail lên Cloudinary và trả về URL. Chỉ Nhân viên y tế hoặc Quản trị viên mới có thể sử dụng.")
    @ApiResponse(responseCode = "200", description = "Upload thành công",
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"thumbnailUrl\": \"https://res.cloudinary.com/...\"}")))
    @ApiResponse(responseCode = "400", description = "File không hợp lệ", content = @Content)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff')")
    @PostMapping(value = "/upload-thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadThumbnail(
            @Parameter(description = "File ảnh thumbnail (jpg, jpeg, png, gif, webp)")
            @RequestParam("file") MultipartFile file) {
        log.info("API POST /api/blogs/upload-thumbnail được gọi để upload thumbnail");
        String thumbnailUrl = blogService.uploadThumbnail(file);
        return ResponseEntity.ok(Map.of("thumbnailUrl", thumbnailUrl));
    }

    @Operation(summary = "Xóa thumbnail từ Cloudinary",
            description = "Xóa ảnh thumbnail khỏi Cloudinary bằng URL. Chỉ Nhân viên y tế hoặc Quản trị viên mới có thể sử dụng.")
    @ApiResponse(responseCode = "200", description = "Xóa thành công")
    @ApiResponse(responseCode = "400", description = "URL không hợp lệ", content = @Content)
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff')")
    @DeleteMapping("/delete-thumbnail")
    public ResponseEntity<Map<String, String>> deleteThumbnail(
            @Parameter(description = "URL của thumbnail cần xóa")
            @RequestParam("thumbnailUrl") String thumbnailUrl) {
        log.info("API DELETE /api/blogs/delete-thumbnail được gọi để xóa thumbnail: {}", thumbnailUrl);
        blogService.deleteThumbnail(thumbnailUrl);
        return ResponseEntity.ok(Map.of("message", "Thumbnail đã được xóa thành công"));
    }
}
