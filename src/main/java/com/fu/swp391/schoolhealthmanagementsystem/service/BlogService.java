package com.fu.swp391.schoolhealthmanagementsystem.service;

import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.BlogResponseDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.CreateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.dto.blog.UpdateBlogRequestDto;
import com.fu.swp391.schoolhealthmanagementsystem.entity.Blog;
import com.fu.swp391.schoolhealthmanagementsystem.entity.User;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.BlogStatus;
import com.fu.swp391.schoolhealthmanagementsystem.entity.enums.UserRole;
import com.fu.swp391.schoolhealthmanagementsystem.exception.ResourceNotFoundException;
import com.fu.swp391.schoolhealthmanagementsystem.mapper.BlogMapper;
import com.fu.swp391.schoolhealthmanagementsystem.repository.BlogRepository;
import com.fu.swp391.schoolhealthmanagementsystem.repository.specification.BlogSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final AuthorizationService authorizationService;
    private final BlogSpecification blogSpecification;

    @Transactional
    public BlogResponseDto createBlog(CreateBlogRequestDto createDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Blog blog = blogMapper.toEntity(createDto, currentUser);
        Blog savedBlog = blogRepository.save(blog);
        return blogMapper.toResponseDto(savedBlog);
    }

    @Transactional(readOnly = true)
    public Page<BlogResponseDto> getAllBlogs(String title, Long authorId, BlogStatus status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Optional<User> currentUserOpt = authorizationService.tryGetCurrentUser();

        Specification<Blog> spec = Specification.allOf(blogSpecification.titleContains(title))
                .and(blogSpecification.hasAuthorId(authorId))
                .and(blogSpecification.hasStatus(status))
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

    @Transactional
    public BlogResponseDto updateBlog(Long blogId, UpdateBlogRequestDto updateDto) {
        User currentUser = authorizationService.getCurrentUserAndValidate();
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        boolean isAuthor = blog.getAuthor().getUserId().equals(currentUser.getUserId());

        if (!isAuthor) {
            throw new AccessDeniedException("Bạn chỉ có thể cập nhật bài đăng của chính mình.");
        }

        blogMapper.updateEntityFromDto(updateDto, blog);
        Blog updatedBlog = blogRepository.save(blog);
        return blogMapper.toResponseDto(updatedBlog);
    }

    @Transactional
    public BlogResponseDto updateBlogStatus(Long blogId, BlogStatus newStatus) {
        // Quyền admin đã được kiểm tra ở controller
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài đăng với ID: " + blogId));

        blog.setStatus(newStatus);
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

        blogRepository.delete(blog);
    }

    private boolean hasAdminOrManagerRole(User user) {
        return user.getRole() == UserRole.SchoolAdmin || user.getRole() == UserRole.StaffManager;
    }
}