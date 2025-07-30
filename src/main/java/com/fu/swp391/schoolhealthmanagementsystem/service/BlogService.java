package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.BlogResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.CreateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.UpdateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.UpdateBlogStatusRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Blog;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogCategory;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.AppException;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.BlogMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.BlogRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.BlogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final AuthorizationService authorizationService;
    private final BlogSpecification blogSpecification;
//    private final NotificationService notificationService;
    private final CloudinaryStorageService cloudinaryStorageService;

    /**
     * Tạo blog mới
     */
    @Transactional
    public BlogResponseDto createBlog(CreateBlogRequestDto createDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[BLOG] Người dùng '{}' đang tạo blog mới với tiêu đề: '{}'", currentUser.getEmail(), createDto.title());
        Blog blog = blogMapper.toEntity(createDto, currentUser);
        Blog savedBlog = blogRepository.save(blog);
        log.info("[BLOG] Tạo blog thành công. ID: {}, Slug: {}", savedBlog.getId(), savedBlog.getSlug());
        return blogMapper.toResponseDto(savedBlog);
    }

    /**
     * Lấy danh sách blog với các bộ lọc
     */
    @Transactional(readOnly = true)
    public Page<BlogResponseDto> getAllBlogs(String search, String title, String description, Long authorId, BlogStatus status, BlogCategory category, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.info("[BLOG] Lấy danh sách blog với các tham số: search='{}', title='{}', description='{}', authorId={}, status={}, category={}, startDate={}, endDate={}, pageable={}",
                search, title, description, authorId, status, category, startDate, endDate, pageable);

        Specification<Blog> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (search != null && !search.isBlank()) {
            log.info("[BLOG] Áp dụng tìm kiếm tổng hợp với từ khóa: '{}'", search);
            spec = spec.and(blogSpecification.searchInTitleDescriptionContent(search));
        } else {
            log.info("[BLOG] Áp dụng bộ lọc tiêu đề và mô tả: title='{}', description='{}'", title, description);
            spec = spec.and(blogSpecification.titleContains(title))
                    .and(blogSpecification.descriptionContains(description));
        }

        log.info("[BLOG] Áp dụng các bộ lọc khác: authorId={}, status={}, category={}, startDate={}, endDate={}",
                authorId, status, category, startDate, endDate);

        spec = spec.and(blogSpecification.hasAuthorId(authorId))
                .and(blogSpecification.hasStatus(status))
                .and(blogSpecification.hasCategory(category))
                .and(blogSpecification.updatedBetween(startDate, endDate));

        Optional<User> currentUserOpt = authorizationService.tryGetCurrentUser();

        if (currentUserOpt.isEmpty() || !hasAdminOrManagerRole(currentUserOpt.get())) {
            log.info("[BLOG] Người dùng không đăng nhập hoặc không có quyền quản trị, chỉ hiển thị các bài đăng công khai.");
            spec = spec.and(blogSpecification.hasStatus(BlogStatus.PUBLIC));
        }
        if (authorId != null && (currentUserOpt.isEmpty() || !hasAdminOrManagerRole(currentUserOpt.get()))) {
            log.warn("[BLOG] Người dùng không có quyền lọc bài đăng theo tác giả.");
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền lọc bài đăng theo tác giả.");
        }

        Page<BlogResponseDto> result = blogRepository.findAll(spec, pageable).map(blogMapper::toResponseDto);
        log.info("[BLOG] Đã trả về {} blog theo tiêu chí tìm kiếm.", result.getTotalElements());
        return result;
    }

    /**
     * Lấy danh sách blog của người dùng hiện tại
     */
    @Transactional(readOnly = true)
    public Page<BlogResponseDto> getMyBlogs(Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[BLOG] Lấy danh sách blog của người dùng '{}', pageable={}", currentUser.getEmail(), pageable);
        Specification<Blog> spec = blogSpecification.hasAuthor(currentUser);
        return blogRepository.findAll(spec, pageable).map(blogMapper::toResponseDto);
    }

    /**
     * Lấy chi tiết blog theo ID
     */
    @Transactional(readOnly = true)
    public BlogResponseDto getBlogById(Long blogId) {
        log.info("[BLOG] Lấy chi tiết blog theo ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đăng với ID: " + blogId));
        if (blog.getStatus() != BlogStatus.PUBLIC) {
            User currentUser = authorizationService.tryGetCurrentUser()
                    .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Bạn phải đăng nhập để xem nội dung này."));
            boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
            boolean isAdminOrManager = hasAdminOrManagerRole(currentUser);
            if (!isAuthor && !isAdminOrManager) {
                log.warn("[BLOG] Người dùng '{}' không có quyền xem blog ID: {}", currentUser.getEmail(), blogId);
                throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem bài đăng này.");
            }
        }
        return blogMapper.toResponseDto(blog);
    }

    /**
     * Lấy chi tiết blog theo slug
     */
    @Transactional(readOnly = true)
    public BlogResponseDto getBlogBySlug(String slug) {
        log.info("[BLOG] Lấy chi tiết blog theo slug: {}", slug);
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đăng với slug: " + slug));
        if (blog.getStatus() != BlogStatus.PUBLIC) {
            User currentUser = authorizationService.tryGetCurrentUser()
                    .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Bạn phải đăng nhập để xem nội dung này."));
            boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
            boolean isAdminOrManager = hasAdminOrManagerRole(currentUser);
            if (!isAuthor && !isAdminOrManager) {
                log.warn("[BLOG] Người dùng '{}' không có quyền xem blog slug: {}", currentUser.getEmail(), slug);
                throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem bài đăng này.");
            }
        }
        return blogMapper.toResponseDto(blog);
    }

    /**
     * Cập nhật blog (chỉ tác giả mới được cập nhật)
     */
    @Transactional
    public BlogResponseDto updateBlog(Long blogId, UpdateBlogRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[BLOG] Người dùng '{}' đang cập nhật blog ID: {}", currentUser.getEmail(), blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đăng với ID: " + blogId));
        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
        if (!isAuthor) {
            log.warn("[BLOG] Người dùng '{}' cố gắng cập nhật blog ID: {} nhưng không phải tác giả", currentUser.getEmail(), blogId);
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn chỉ có thể cập nhật bài đăng của chính mình.");
        }
        if (updateDto.thumbnail() != null && !updateDto.thumbnail().equals(blog.getThumbnail())) {
            if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
                try {
                    deleteThumbnailByUrl(blog.getThumbnail());
                } catch (Exception e) {
                    log.warn("[BLOG] Không thể xóa thumbnail cũ: {} cho blog ID: {}. Lỗi: {}", blog.getThumbnail(), blogId, e.getMessage());
                }
            }
        }
        blogMapper.updateEntityFromDto(updateDto, blog);
        Blog updatedBlog = blogRepository.save(blog);
        log.info("[BLOG] Cập nhật blog ID: {} thành công bởi người dùng '{}'", blogId, currentUser.getEmail());
        return blogMapper.toResponseDto(updatedBlog);
    }

    /**
     * Xóa blog (tác giả, admin, manager mới được xóa)
     */
    @Transactional
    public void deleteBlog(Long blogId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.info("[BLOG] Người dùng '{}' đang xóa blog ID: {}", currentUser.getEmail(), blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đăng với ID: " + blogId));
        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.SchoolAdmin);
        boolean isManager = currentUser.getRole().equals(UserRole.StaffManager);
        if (!isAdmin && !isAuthor && !isManager) {
            log.warn("[BLOG] Người dùng '{}' cố gắng xóa blog ID: {} nhưng không có quyền", currentUser.getEmail(), blogId);
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền xóa bài đăng này.");
        }
        if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
            try {
                deleteThumbnailByUrl(blog.getThumbnail());
            } catch (Exception e) {
                log.warn("[BLOG] Không thể xóa thumbnail khi xóa blog ID: {}: {}", blogId, blog.getThumbnail(), e.getMessage());
            }
        }
        blogRepository.delete(blog);
        log.info("[BLOG] Blog ID: {} đã bị xóa bởi người dùng '{}'", blogId, currentUser.getEmail());
    }

    /**
     * Cập nhật trạng thái blog (chỉ admin/manager)
     */
    @Transactional
    public BlogResponseDto updateBlogStatus(Long blogId, UpdateBlogStatusRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        if (!hasAdminOrManagerRole(currentUser)) {
            log.warn("[BLOG] Người dùng '{}' cố gắng cập nhật trạng thái blog ID: {} nhưng không có quyền", currentUser.getEmail(), blogId);
            throw new AppException(HttpStatus.FORBIDDEN, "Bạn không có quyền cập nhật trạng thái bài đăng.");
        }
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy bài đăng với ID: " + blogId));
        blog.setStatus(updateDto.status());
        Blog updatedBlog = blogRepository.save(blog);
        log.info("[BLOG] Trạng thái blog ID: {} đã được cập nhật thành {} bởi người dùng '{}'", blogId, updateDto.status(), currentUser.getEmail());
        return blogMapper.toResponseDto(updatedBlog);
    }

    /**
     * Upload thumbnail cho blog
     */
    public String uploadThumbnail(MultipartFile file) {
        authorizationService.getCurrentUserAndValidate();
        log.info("[BLOG] Đang upload thumbnail cho blog");
        if (file == null || file.isEmpty()) {
            log.warn("[BLOG] File thumbnail không được để trống");
            throw new AppException(HttpStatus.BAD_REQUEST, "File thumbnail không được để trống");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("[BLOG] File phải là ảnh (jpg, jpeg, png, gif, webp)");
            throw new AppException(HttpStatus.BAD_REQUEST, "File phải là ảnh (jpg, jpeg, png, gif, webp)");
        }
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            log.warn("[BLOG] Kích thước file không được vượt quá 5MB");
            throw new AppException(HttpStatus.BAD_REQUEST, "Kích thước file không được vượt quá 5MB");
        }
        try {
            String thumbnailUrl = cloudinaryStorageService.uploadBlogThumbnail(file);
            log.info("[BLOG] Upload thumbnail thành công: {}", thumbnailUrl);
            return thumbnailUrl;
        } catch (Exception e) {
            log.error("[BLOG] Lỗi khi upload thumbnail", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể upload thumbnail: " + e.getMessage());
        }
    }

    /**
     * Xóa thumbnail blog
     */
    public void deleteThumbnail(String thumbnailUrl) {
        authorizationService.getCurrentUserAndValidate();
        log.info("[BLOG] Đang xóa thumbnail: {}", thumbnailUrl);
        deleteThumbnailByUrl(thumbnailUrl);
    }

    private void deleteThumbnailByUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            log.warn("[BLOG] Không có thumbnail để xóa hoặc URL rỗng.");
            return;
        }
        try {
            String publicId = extractPublicIdFromUrl(thumbnailUrl);
            if (publicId != null) {
                cloudinaryStorageService.deleteEditorImage(publicId);
                log.info("[BLOG] Đã xóa thumbnail: {} (publicId: {})", thumbnailUrl, publicId);
            } else {
                log.warn("[BLOG] Không thể extract publicId từ URL thumbnail: {}", thumbnailUrl);
            }
        } catch (Exception e) {
            log.error("[BLOG] Lỗi khi xóa thumbnail từ Cloudinary: {} - {}", thumbnailUrl, e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Không thể xóa thumbnail: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            log.warn("[BLOG] URL thumbnail rỗng khi extract publicId.");
            return null;
        }
        try {
            if (!url.contains("cloudinary.com")) {
                log.warn("[BLOG] URL không phải từ Cloudinary: {}", url);
                return null;
            }
            String uploadMarker = "/upload/";
            int uploadIndex = url.indexOf(uploadMarker);
            if (uploadIndex == -1) {
                log.warn("[BLOG] Không tìm thấy '/upload/' trong URL: {}", url);
                return null;
            }
            String afterUpload = url.substring(uploadIndex + uploadMarker.length());
            if (afterUpload.matches("^v\\d+/.*")) {
                int slashIndex = afterUpload.indexOf('/');
                if (slashIndex != -1) {
                    afterUpload = afterUpload.substring(slashIndex + 1);
                }
            }
            int lastDotIndex = afterUpload.lastIndexOf('.');
            if (lastDotIndex != -1) {
                afterUpload = afterUpload.substring(0, lastDotIndex);
            }
            log.debug("[BLOG] Extracted publicId '{}' từ URL: {}", afterUpload, url);
            return afterUpload;
        } catch (Exception e) {
            log.error("[BLOG] Lỗi khi extract public_id từ URL: {} - {}", url, e.getMessage(), e);
            return null;
        }
    }

    private boolean hasAdminOrManagerRole(User user) {
        return user.getRole() == UserRole.SchoolAdmin || user.getRole() == UserRole.StaffManager;
    }
}
