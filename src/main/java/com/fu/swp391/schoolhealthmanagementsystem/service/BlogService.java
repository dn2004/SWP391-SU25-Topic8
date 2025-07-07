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
    private final NotificationService notificationService;
    private final CloudinaryStorageService cloudinaryStorageService;

    @Transactional
    public BlogResponseDto createBlog(CreateBlogRequestDto createDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Blog blog = blogMapper.toEntity(createDto, currentUser);
        Blog savedBlog = blogRepository.save(blog);
        return blogMapper.toResponseDto(savedBlog);
    }

    @Transactional(readOnly = true)
    public Page<BlogResponseDto> getAllBlogs(String search, String title, String description, Long authorId, BlogStatus status, BlogCategory category, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Optional<User> currentUserOpt = authorizationService.tryGetCurrentUser();

        // Bắt đầu với specification cơ bản - sử dụng phương thức mới thay vì deprecated
        Specification<Blog> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Nếu có từ khóa tìm kiếm, sử dụng tìm kiếm tổng hợp
        if (search != null && !search.isBlank()) {
            spec = spec.and(blogSpecification.searchInTitleDescriptionContent(search));
        } else {
            // Nếu không có tìm kiếm tổng hợp, áp dụng các bộ lọc riêng lẻ
            spec = spec.and(blogSpecification.titleContains(title))
                    .and(blogSpecification.descriptionContains(description));
        }

        // Áp dụng các bộ lọc khác
        spec = spec.and(blogSpecification.hasAuthorId(authorId))
                .and(blogSpecification.hasStatus(status))
                .and(blogSpecification.hasCategory(category))
                .and(blogSpecification.updatedBetween(startDate, endDate));

        // Nếu người dùng không đăng nhập hoặc không phải admin/manager, chỉ hiển thị các bài PUBLIC
        if (currentUserOpt.isEmpty() || !hasAdminOrManagerRole(currentUserOpt.get())) {
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
        Specification<Blog> spec = blogSpecification.hasAuthor(currentUser);
        return blogRepository.findAll(spec, pageable).map(blogMapper::toResponseDto);
    }

    @Transactional(readOnly = true)
    public BlogResponseDto getBlogById(Long blogId) {
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
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());

        if (!isAuthor) {
            throw new AccessDeniedException("Bạn chỉ có thể cập nhật bài đăng của chính mình.");
        }

        // Xử lý thay đổi thumbnail
        if (updateDto.thumbnail() != null && !updateDto.thumbnail().equals(blog.getThumbnail())) {
            // Xóa thumbnail cũ nếu có
            if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
                try {
                    deleteThumbnailByUrl(blog.getThumbnail());
                } catch (Exception e) {
                    log.warn("Không thể xóa thumbnail cũ: {}", blog.getThumbnail(), e);
                }
            }
        }

        blogMapper.updateEntityFromDto(updateDto, blog);
        Blog updatedBlog = blogRepository.save(blog);
        return blogMapper.toResponseDto(updatedBlog);
    }

    @Transactional
    public void deleteBlog(Long blogId) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.SchoolAdmin);

        if (!isAdmin && !isAuthor) {
            throw new AccessDeniedException("Bạn không có quyền xóa bài đăng này.");
        }

        // Xóa thumbnail từ Cloudinary trước khi xóa blog
        if (blog.getThumbnail() != null && !blog.getThumbnail().isEmpty()) {
            try {
                deleteThumbnailByUrl(blog.getThumbnail());
            } catch (Exception e) {
                log.warn("Không thể xóa thumbnail khi xóa blog: {}", blog.getThumbnail(), e);
            }
        }

        blogRepository.delete(blog);
    }

    @Transactional
    public BlogResponseDto updateBlogStatus(Long blogId, UpdateBlogStatusRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        // This action is restricted to Admins and Managers
        if (!hasAdminOrManagerRole(currentUser)) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái bài đăng.");
        }

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        blog.setStatus(updateDto.status());
        Blog updatedBlog = blogRepository.save(blog);

        log.info("Trạng thái của bài đăng ID {} đã được cập nhật thành {} bởi người dùng {}", blogId, updateDto.status(), currentUser.getEmail());

        return blogMapper.toResponseDto(updatedBlog);
    }

    public String uploadThumbnail(MultipartFile file) {
        authorizationService.getCurrentUserAndValidate();

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
            log.info("Thumbnail đã được upload thành công: {}", thumbnailUrl);
            return thumbnailUrl;
        } catch (Exception e) {
            log.error("Lỗi khi upload thumbnail", e);
            throw new RuntimeException("Không thể upload thumbnail: " + e.getMessage());
        }
    }

    public void deleteThumbnail(String thumbnailUrl) {
        authorizationService.getCurrentUserAndValidate();
        deleteThumbnailByUrl(thumbnailUrl);
    }

    private void deleteThumbnailByUrl(String thumbnailUrl) {
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            return;
        }

        try {
            // Extract public_id from Cloudinary URL
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
            // URL format: https://res.cloudinary.com/{cloud_name}/{resource_type}/upload/{public_id}.{format}
            // Example: https://res.cloudinary.com/mycloud/image/upload/v1234567890/folder/filename.jpg

            if (!url.contains("cloudinary.com")) {
                log.warn("URL không phải từ Cloudinary: {}", url);
                return null;
            }

            // Find the part after '/upload/'
            String uploadMarker = "/upload/";
            int uploadIndex = url.indexOf(uploadMarker);
            if (uploadIndex == -1) {
                log.warn("Không tìm thấy '/upload/' trong URL: {}", url);
                return null;
            }

            String afterUpload = url.substring(uploadIndex + uploadMarker.length());

            // Remove version if present (starts with 'v' followed by numbers)
            if (afterUpload.matches("^v\\d+/.*")) {
                int slashIndex = afterUpload.indexOf('/');
                if (slashIndex != -1) {
                    afterUpload = afterUpload.substring(slashIndex + 1);
                }
            }

            // Remove file extension
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
