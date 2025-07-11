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
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.BlogMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.BlogRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.BlogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    @Transactional
    public BlogResponseDto createBlog(CreateBlogRequestDto createDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.debug("Người dùng '{}' đang tạo blog mới với tiêu đề: '{}'", currentUser.getEmail(), createDto.title());
        Blog blog = blogMapper.toEntity(createDto, currentUser);
        Blog savedBlog = blogRepository.save(blog);
        log.info("Tạo blog thành công. ID: {}, Slug: {}", savedBlog.getId(), savedBlog.getSlug());
        return blogMapper.toResponseDto(savedBlog);
    }

    @Transactional(readOnly = true)
    public Page<BlogResponseDto> getAllBlogs(String search, String title, String description, Long authorId, BlogStatus status, BlogCategory category, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        log.debug("Lấy danh sách blog với các tham số: search='{}', title='{}', description='{}', authorId={}, status={}, category={}, startDate={}, endDate={}, pageable={}",
                search, title, description, authorId, status, category, startDate, endDate, pageable);

        Specification<Blog> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        if (search != null && !search.isBlank()) {
            log.info("Áp dụng tìm kiếm tổng hợp với từ khóa: '{}'", search);
            spec = spec.and(blogSpecification.searchInTitleDescriptionContent(search));
        } else {
            log.info("Áp dụng bộ lọc tiêu đề và mô tả: title='{}', description='{}'", title, description);
            spec = spec.and(blogSpecification.titleContains(title))
                    .and(blogSpecification.descriptionContains(description));
        }

        log.info("Áp dụng các bộ lọc khác: authorId={}, status={}, category={}, startDate={}, endDate={}",
                authorId, status, category, startDate, endDate);

        spec = spec.and(blogSpecification.hasAuthorId(authorId))
                .and(blogSpecification.hasStatus(status))
                .and(blogSpecification.hasCategory(category))
                .and(blogSpecification.updatedBetween(startDate, endDate));

        // Kiểm tra quyền truy cập
        Optional<User> currentUserOpt = authorizationService.tryGetCurrentUser();

        // Nếu người dùng không đăng nhập hoặc không phải admin/manager, chỉ hiển thị các bài PUBLIC
        if (currentUserOpt.isEmpty() || !hasAdminOrManagerRole(currentUserOpt.get())) {
            log.info("Người dùng không đăng nhập hoặc không có quyền quản trị, chỉ hiển thị các bài đăng công khai.");
            spec = spec.and(blogSpecification.hasStatus(BlogStatus.PUBLIC));
        }

        // Nếu người dùng yêu cầu xem theo authorId, phải là admin/manager
        if (authorId != null && (currentUserOpt.isEmpty() || !hasAdminOrManagerRole(currentUserOpt.get()))) {
            throw new AccessDeniedException("Bạn không có quyền lọc bài đăng theo tác giả.");
        }

        return blogRepository.findAll(spec, pageable).map(blogMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public Page<BlogResponseDto> getMyBlogs(Pageable pageable) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.debug("Lấy danh sách blog của người dùng '{}', pageable={}", currentUser.getEmail(), pageable);
        Specification<Blog> spec = blogSpecification.hasAuthor(currentUser);
        return blogRepository.findAll(spec, pageable).map(blogMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public BlogResponseDto getBlogById(Long blogId) {
        log.debug("Lấy chi tiết blog theo ID: {}", blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        // Nếu blog không public, kiểm tra quyền
        if (blog.getStatus() != BlogStatus.PUBLIC) {
            User currentUser = authorizationService.tryGetCurrentUser()
                    .orElseThrow(() -> new AccessDeniedException("Bạn phải đăng nhập để xem nội dung này."));

            boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
            boolean isAdminOrManager = hasAdminOrManagerRole(currentUser);

            if (!isAuthor && !isAdminOrManager) {
                throw new AccessDeniedException("Bạn không có quyền xem bài đăng này.");
            }
        }

        return blogMapper.toResponseDto(blog);
    }

    @Transactional(readOnly = true)
    public BlogResponseDto getBlogBySlug(String slug) {
        log.debug("Lấy chi tiết blog theo slug: {}", slug);
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với slug: " + slug));

        // Nếu blog không public, kiểm tra quyền
        if (blog.getStatus() != BlogStatus.PUBLIC) {
            User currentUser = authorizationService.tryGetCurrentUser()
                    .orElseThrow(() -> new AccessDeniedException("Bạn phải đăng nhập để xem nội dung này."));

            boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
            boolean isAdminOrManager = hasAdminOrManagerRole(currentUser);

            if (!isAuthor && !isAdminOrManager) {
                throw new AccessDeniedException("Bạn không có quyền xem bài đăng này.");
            }
        }

        return blogMapper.toResponseDto(blog);
    }

    @Transactional
    public BlogResponseDto updateBlog(Long blogId, UpdateBlogRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.debug("Người dùng '{}' đang cập nhật blog ID: {}", currentUser.getEmail(), blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());

        if (!isAuthor) {
            log.warn("Người dùng '{}' cố gắng cập nhật blog ID: {} nhưng không phải tác giả", currentUser.getEmail(), blogId);
            throw new AccessDeniedException("Bạn chỉ có thể cập nhật bài đăng của chính mình.");
        }

        // Xử lý thay đổi thumbnail
        if (updateDto.thumbnail() != null && !updateDto.thumbnail().equals(blog.getThumbnail())) {
            if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
                try {
                    deleteThumbnailByUrl(blog.getThumbnail());
                } catch (Exception e) {
                    log.warn("Không thể xóa thumbnail cũ: {} cho blog ID: {}", blog.getThumbnail(), blogId, e);
                }
            }
        }

        blogMapper.updateEntityFromDto(updateDto, blog);
        Blog updatedBlog = blogRepository.save(blog);
        log.info("Cập nhật blog ID: {} thành công bởi người dùng '{}'", blogId, currentUser.getEmail());
        return blogMapper.toResponseDto(updatedBlog);
    }

    @Transactional
    public void deleteBlog(Long blogId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        log.debug("Người dùng '{}' đang xóa blog ID: {}", currentUser.getEmail(), blogId);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.SchoolAdmin);

        if (!isAdmin && !isAuthor) {
            log.warn("Người dùng '{}' cố gắng xóa blog ID: {} nhưng không có quyền", currentUser.getEmail(), blogId);
            throw new AccessDeniedException("Bạn không có quyền xóa bài đăng này.");
        }

        if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
            try {
                deleteThumbnailByUrl(blog.getThumbnail());
            } catch (Exception e) {
                log.warn("Không thể xóa thumbnail khi xóa blog ID: {}: {}", blogId, blog.getThumbnail(), e);
            }
        }

        blogRepository.delete(blog);
        log.info("Blog ID: {} đã bị xóa bởi người dùng '{}'", blogId, currentUser.getEmail());
    }

    @Transactional
    public BlogResponseDto updateBlogStatus(Long blogId, UpdateBlogStatusRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        if (!hasAdminOrManagerRole(currentUser)) {
            log.warn("Người dùng '{}' cố gắng cập nhật trạng thái blog ID: {} nhưng không có quyền", currentUser.getEmail(), blogId);
            throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái bài đăng.");
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        blog.setStatus(updateDto.status());
        Blog updatedBlog = blogRepository.save(blog);

        log.info("Trạng thái blog ID: {} đã được cập nhật thành {} bởi người dùng '{}'", blogId, updateDto.status(), currentUser.getEmail());
        return blogMapper.toResponseDto(updatedBlog);
    }

    public String uploadThumbnail(MultipartFile file) {
        authorizationService.getCurrentUserAndValidate();
        log.debug("Đang upload thumbnail cho blog");
        // Validate file type
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File thumbnail không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là ảnh (jpg, jpeg, png, gif, webp)");
        }

        // Validate file size (max 5MB)
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        try {
            String thumbnailUrl = cloudinaryStorageService.uploadBlogThumbnail(file);
            log.info("Upload thumbnail thành công: {}", thumbnailUrl);
            return thumbnailUrl;
        } catch (Exception e) {
            log.error("Lỗi khi upload thumbnail", e);
            throw new RuntimeException("Không thể upload thumbnail: " + e.getMessage());
        }
    }

    public void deleteThumbnail(String thumbnailUrl) {
        authorizationService.getCurrentUserAndValidate();
        log.debug("Đang xóa thumbnail: {}", thumbnailUrl);
        deleteThumbnailByUrl(thumbnailUrl);
    }

    private void deleteThumbnailByUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            return;
        }
        try {
            String publicId = extractPublicIdFromUrl(thumbnailUrl);
            if (publicId != null) {
                cloudinaryStorageService.deleteEditorImage(publicId);
                log.info("Đã xóa thumbnail: {}", thumbnailUrl);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa thumbnail từ Cloudinary: {}", thumbnailUrl, e);
            throw new RuntimeException("Không thể xóa thumbnail: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            if (!url.contains("cloudinary.com")) {
                log.warn("URL không phải từ Cloudinary: {}", url);
                return null;
            }
            String uploadMarker = "/upload/";
            int uploadIndex = url.indexOf(uploadMarker);
            if (uploadIndex == -1) {
                log.warn("Không tìm thấy '/upload/' trong URL: {}", url);
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

            return afterUpload;
        } catch (Exception e) {
            log.error("Lỗi khi extract public_id từ URL: {}", url, e);
            return null;
        }
    }

    private boolean hasAdminOrManagerRole(User user) {
        return user.getRole() == UserRole.SchoolAdmin || user.getRole() == UserRole.StaffManager;
    }
}
