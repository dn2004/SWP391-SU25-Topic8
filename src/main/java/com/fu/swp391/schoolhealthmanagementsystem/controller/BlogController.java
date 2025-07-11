package com.fu.swp391.schoolhealthmanagementsystem.controller;

import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.BlogResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.CreateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.UpdateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.UpdateBlogStatusRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import com.fu.swp391.schoolhealthmanagementsystem.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Blog Management", description = "API để quản lý các bài đăng trên blog")
@SecurityRequirement(name = "bearerAuth")
public class BlogController {

    private final BlogService blogService;

    @Operation(
            summary = "Tạo một bài blog mới",
            description = """
                    ### Mô tả
                    Tạo một bài đăng blog mới. Người dùng hiện tại sẽ được gán làm tác giả.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin`, `MedicalStaff`, hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @PostMapping
    public ResponseEntity<BlogResponseDto> createBlog(@Valid @RequestBody CreateBlogRequestDto createBlogRequestDto) {
        log.info("API POST /api/blogs được gọi để tạo bài đăng mới với tiêu đề: '{}'", createBlogRequestDto.title());
        BlogResponseDto createdBlog = blogService.createBlog(createBlogRequestDto);
        return new ResponseEntity<>(createdBlog, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Lấy danh sách tất cả bài blog (công khai, có phân trang)",
            description = """
                    ### Mô tả
                    Lấy danh sách các bài đăng trên blog. Hỗ trợ tìm kiếm và lọc theo nhiều tiêu chí.
                    - **Phân quyền:** 
                        - **Người dùng công khai:** Chỉ xem được các bài đăng có trạng thái `PUBLIC`.
                        - **`SchoolAdmin`, `StaffManager`:** Có thể xem tất cả các trạng thái và lọc theo ID tác giả.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping()
    public ResponseEntity<Page<BlogResponseDto>> getAllBlogs(
            @Parameter(description = "Tìm kiếm tổng hợp trong tiêu đề, mô tả và nội dung") @RequestParam(required = false) String search,
            @Parameter(description = "Lọc theo tiêu đề bài đăng") @RequestParam(required = false) String title,
            @Parameter(description = "Lọc theo mô tả") @RequestParam(required = false) String description,
            @Parameter(description = "(Admin/Manager) Lọc theo ID của tác giả") @RequestParam(required = false) Long authorId,
            @Parameter(description = "(Admin/Manager) Lọc theo trạng thái") @RequestParam(required = false) BlogStatus status,
            @Parameter(description = "Lọc theo danh mục") @RequestParam(required = false) BlogCategory category,
            @Parameter(description = "Lọc từ ngày cập nhật (YYYY-MM-DD)") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "Lọc đến ngày cập nhật (YYYY-MM-DD)") @RequestParam(required = false) LocalDate endDate,
            @ParameterObject @PageableDefault(size = 10, sort = "updatedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API GET /api/blogs (phân trang) được gọi với pageable: {}", pageable);
        Page<BlogResponseDto> blogsPage = blogService.getAllBlogs(search, title, description, authorId, status, category, startDate, endDate, pageable);
        return ResponseEntity.ok(blogsPage);
    }

    @Operation(
            summary = "Lấy danh sách các bài blog của tôi (phân trang)",
            description = """
                    ### Mô tả
                    Lấy danh sách các bài đăng được tạo bởi chính người dùng đang đăng nhập.
                    - **Phân quyền:** Yêu cầu người dùng đã xác thực.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/my-blogs")
    public ResponseEntity<Page<BlogResponseDto>> getMyBlogs(
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API GET /api/blogs/my-blogs được gọi");
        Page<BlogResponseDto> blogsPage = blogService.getMyBlogs(pageable);
        return ResponseEntity.ok(blogsPage);
    }

    @Operation(
            summary = "(Admin/Manager) Lấy danh sách các bài blog của một tác giả",
            description = """
                    ### Mô tả
                    Lấy danh sách các bài đăng được tạo bởi một tác giả cụ thể.
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager')")
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<BlogResponseDto>> getBlogsByAuthor(
            @Parameter(description = "ID của tác giả") @PathVariable Long authorId,
            @ParameterObject @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("API GET /api/blogs/author/{} được gọi", authorId);
        // Tái sử dụng phương thức getAllBlogs với bộ lọc authorId
        Page<BlogResponseDto> blogsPage = blogService.getAllBlogs(null, null, null, authorId, null, null, null, null, pageable);
        return ResponseEntity.ok(blogsPage);
    }

    @Operation(
            summary = "Lấy thông tin chi tiết một bài blog bằng ID (công khai)",
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của một bài đăng bằng ID.
                    - **Phân quyền:** 
                        - **Người dùng công khai:** Chỉ xem được bài đăng có trạng thái `PUBLIC`.
                        - **Tác giả, `SchoolAdmin`, `StaffManager`:** Có thể xem các trạng thái khác.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem bài đăng này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài đăng", content = @Content)
    })
    @GetMapping("/{blogId}")
    public ResponseEntity<BlogResponseDto> getBlogById(
            @Parameter(description = "ID của bài đăng") @PathVariable Long blogId) {
        log.info("API GET /api/blogs/{} được gọi", blogId);
        BlogResponseDto blogDto = blogService.getBlogById(blogId);
        return ResponseEntity.ok(blogDto);
    }

    @Operation(
            summary = "Lấy thông tin chi tiết một bài blog bằng slug (công khai)",
            description = """
                    ### Mô tả
                    Lấy thông tin chi tiết của một bài đăng bằng slug (đường dẫn thân thiện).
                    - **Phân quyền:** 
                        - **Người dùng công khai:** Chỉ xem được bài đăng có trạng thái `PUBLIC`.
                        - **Tác giả, `SchoolAdmin`, `StaffManager`:** Có thể xem các trạng thái khác.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền xem bài đăng này", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài đăng", content = @Content)
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<BlogResponseDto> getBlogBySlug(
            @Parameter(description = "Slug của bài đăng") @PathVariable String slug) {
        log.info("API GET /api/blogs/slug/{} được gọi", slug);
        BlogResponseDto blogDto = blogService.getBlogBySlug(slug);
        return ResponseEntity.ok(blogDto);
    }

    @Operation(
            summary = "Cập nhật một bài blog",
            description = """
                    ### Mô tả
                    Cập nhật nội dung của một bài đăng.
                    - **Phân quyền:** Chỉ tác giả của bài đăng mới có thể cập nhật.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài đăng", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff')") // Vẫn cần quyền cơ bản để viết blog
    @PutMapping("/{blogId}")
    public ResponseEntity<BlogResponseDto> updateBlog(
            @Parameter(description = "ID của bài đăng cần cập nhật") @PathVariable Long blogId,
            @Valid @RequestBody UpdateBlogRequestDto updateBlogRequestDto) {
        log.info("API PUT /api/blogs/{} được gọi", blogId);
        BlogResponseDto updatedBlog = blogService.updateBlog(blogId, updateBlogRequestDto);
        return ResponseEntity.ok(updatedBlog);
    }

    @Operation(
            summary = "(Admin/Manager) Cập nhật trạng thái một bài blog",
            description = """
                    ### Mô tả
                    Cập nhật trạng thái của một bài đăng (ví dụ: duyệt bài từ `DRAFT` sang `PUBLIC`).
                    - **Phân quyền:** Yêu cầu vai trò `SchoolAdmin` hoặc `StaffManager`.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BlogResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài đăng", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'StaffManager', 'MedicalStaff')")
    @PatchMapping("/{blogId}/status")
    public ResponseEntity<BlogResponseDto> updateBlogStatus(
            @Parameter(description = "ID của bài đăng cần cập nhật trạng thái") @PathVariable Long blogId,
            @Valid @RequestBody UpdateBlogStatusRequestDto updateBlogStatusRequestDto) {
        log.info("API PATCH /api/blogs/{}/status được gọi", blogId);
        BlogResponseDto updatedBlog = blogService.updateBlogStatus(blogId, updateBlogStatusRequestDto);
        return ResponseEntity.ok(updatedBlog);
    }

    @Operation(
            summary = "Xóa một bài blog",
            description = """
                    ### Mô tả
                    Xóa một bài đăng.
                    - **Phân quyền:** Chỉ tác giả của bài đăng hoặc người dùng có vai trò `SchoolAdmin` mới có thể xóa.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công", content = @Content),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bài đăng", content = @Content)
    })
    @PreAuthorize("hasAnyRole('SchoolAdmin', 'MedicalStaff', 'StaffManager')")
    @DeleteMapping("/{blogId}")
    public ResponseEntity<Void> deleteBlog(
            @Parameter(description = "ID của bài đăng cần xóa") @PathVariable Long blogId) {
        log.info("API DELETE /api/blogs/{} được gọi", blogId);
        blogService.deleteBlog(blogId);
        return ResponseEntity.noContent().build();
    }
}
